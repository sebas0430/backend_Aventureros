package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que gestiona la recepción de mensajes por parte de eventos CATCH.
 * Soporta mensajes internos (de otros procesos) y externos (de sistemas fuera del motor).
 *
 * Seguridad:
 * - Para fuentes EXTERNAS se valida el tokenSeguridad contra los conectores configurados.
 * - El payload se sanitiza para prevenir inyección de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCatchService implements IMessageCatchService {

    private final EventoMensajeRepository eventoMensajeRepository;
    private final RecepcionMensajeRepository recepcionMensajeRepository;
    private final ConectorExternoRepository conectorExternoRepository;
    private final EmpresaRepository empresaRepository;
    private final InstanciaProcesoRepository instanciaProcesoRepository;

    @Override
    @Transactional
    public List<RecepcionMensaje> recibirMensaje(MensajeCatchDTO dto) {
        // Validar empresa
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa destino no encontrada"));

        // Sanitizar el payload para prevenir inyección
        String payloadSanitizado = sanitizarPayload(dto.getPayload());

        // Validar credenciales si la fuente es EXTERNA
        boolean credencialValidada;
        if ("EXTERNO".equalsIgnoreCase(dto.getFuente())) {
            credencialValidada = validarCredencialExterna(dto.getTokenSeguridad(), empresa.getId());
            if (!credencialValidada) {
                // Registrar el intento rechazado para auditoría
                log.warn("SEGURIDAD: Intento de Message Catch RECHAZADO. Emisor='{}', Mensaje='{}', Empresa={}. Token inválido.",
                        dto.getIdentificadorEmisor(), dto.getNombreMensaje(), empresa.getId());

                // No podemos guardar sin eventoCatch (FK not null), así que lanzamos excepción directa
                throw new BusinessRuleException(
                        "Credencial/token de seguridad inválido. El mensaje externo ha sido rechazado. " +
                        "Emisor: " + dto.getIdentificadorEmisor());
            }
        } else {
            // Fuente INTERNA: se confía en el sistema (ya autenticado)
            credencialValidada = true;
        }

        // Buscar CATCHes activos que coincidan
        List<EventoMensaje> catchesActivos;
        if (dto.getCorrelationKey() != null && !dto.getCorrelationKey().isBlank()) {
            catchesActivos = eventoMensajeRepository.findActiveCatchesByNombreAndCorrelationAndEmpresa(
                    dto.getNombreMensaje(), dto.getCorrelationKey(), empresa.getId());
        } else {
            catchesActivos = eventoMensajeRepository.findActiveCatchesByNombreAndEmpresa(
                    dto.getNombreMensaje(), empresa.getId());
        }

        if (catchesActivos.isEmpty()) {
            log.warn("MESSAGE CATCH: No hay CATCHes activos para el mensaje '{}' en la empresa {}",
                    dto.getNombreMensaje(), empresa.getId());
            throw new BusinessRuleException(
                    "No hay procesos con un CATCH activo esperando el mensaje '" + dto.getNombreMensaje() + "'.");
        }

        List<RecepcionMensaje> recepciones = new ArrayList<>();

        for (EventoMensaje catchEvento : catchesActivos) {
            String variablesMapeadas = mapearVariables(payloadSanitizado, catchEvento.getPayloadSchema());

            // 1. Buscar las Instancias de Proceso Activas para esta definición de Proceso
            List<InstanciaProceso> instanciasActivas;
            if (dto.getCorrelationKey() != null && catchEvento.getTipoCorrelacion() == ReglaCorrelacion.BUSINESS_KEY) {
                instanciasActivas = instanciaProcesoRepository.findByBusinessKeyAndProcesoIdAndEstado(
                        dto.getCorrelationKey(), catchEvento.getProceso().getId(), EstadoInstancia.ACTIVA);
            } else {
                instanciasActivas = instanciaProcesoRepository.findByProcesoIdAndEstado(
                        catchEvento.getProceso().getId(), EstadoInstancia.ACTIVA);
            }

            // 2. Correlación: Determinar a qué instancias entregar el mensaje
            List<InstanciaProceso> destinos = new ArrayList<>();

            if (instanciasActivas.isEmpty()) {
                if (catchEvento.isCrearInstanciaSiFalla()) {
                    InstanciaProceso nuevaInstancia = InstanciaProceso.builder()
                            .proceso(catchEvento.getProceso())
                            .businessKey(dto.getCorrelationKey())
                            .estado(EstadoInstancia.ACTIVA)
                            .variables(variablesMapeadas)
                            .build();
                    destinos.add(instanciaProcesoRepository.save(nuevaInstancia));
                } else {
                    log.warn("Correlación fallida: No se encontraron instancias para el Catch en el proceso {} y 'crearInstanciaSiFalla' es falso", catchEvento.getProceso().getId());
                    continue; // Skip este catch
                }
            } else if (instanciasActivas.size() == 1) {
                destinos.add(instanciasActivas.get(0));
            } else {
                // Hay múltiples coincidencias: aplicar política
                PoliticaMultiplesCoincidencias politica = catchEvento.getPoliticaMultiples() != null ? 
                        catchEvento.getPoliticaMultiples() : PoliticaMultiplesCoincidencias.ERROR;

                switch (politica) {
                    case ENTREGAR_A_PRIMERA -> destinos.add(instanciasActivas.get(0));
                    case ENTREGAR_A_TODAS -> destinos.addAll(instanciasActivas);
                    case CREAR_NUEVA_INSTANCIA -> {
                        InstanciaProceso nueva = InstanciaProceso.builder()
                                .proceso(catchEvento.getProceso())
                                .businessKey(dto.getCorrelationKey())
                                .estado(EstadoInstancia.ACTIVA)
                                .variables(variablesMapeadas)
                                .build();
                        destinos.add(instanciaProcesoRepository.save(nueva));
                    }
                    case ERROR -> throw new BusinessRuleException("Se encontraron múltiples instancias activas y la política de correlación indica ERROR para el proceso " + catchEvento.getProceso().getId());
                }
            }

            // 3. Registrar la recepción para cada destino
            for (InstanciaProceso in : destinos) {
                // Actualizar variables de la instancia
                if (in.getId() != null) {
                    in.setVariables(variablesMapeadas); // Simple replace for now, in prod merge JSON
                    instanciaProcesoRepository.save(in);
                }

                String accion = (in.getId() != null && !catchEvento.isCrearInstanciaSiFalla() && !instanciasActivas.isEmpty()) 
                        ? "CONTINUAR_INSTANCIA_ID_" + in.getId() 
                        : "ACTIVAR_NUEVA_INSTANCIA_ID_" + in.getId();

                RecepcionMensaje recepcion = RecepcionMensaje.builder()
                        .eventoCatch(catchEvento)
                        .fuenteMensaje(dto.getFuente().toUpperCase())
                        .identificadorEmisor(dto.getIdentificadorEmisor())
                        .payloadRecibido(payloadSanitizado)
                        .variablesMapeadas(variablesMapeadas)
                        .correlationKeyUsada(dto.getCorrelationKey())
                        .credencialValidada(credencialValidada)
                        .accionTomada(accion)
                        .detalle("Entregado a Instancia " + in.getId() + " del Proceso " + catchEvento.getProceso().getId())
                        .build();

                recepciones.add(recepcionMensajeRepository.save(recepcion));

                log.info("AUDITORIA (MESSAGE CATCH): Instancia {} (Proceso {}) recibió mensaje '{}' desde {}. Acción: {}",
                        in.getId(), catchEvento.getProceso().getId(), dto.getNombreMensaje(), dto.getFuente(), accion);
            }
        }

        return recepciones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionMensaje> listarRecepcionesPorProceso(Long procesoId) {
        return recepcionMensajeRepository.findByEventoCatchProcesoId(procesoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionMensaje> listarRecepcionesPorCatch(Long eventoCatchId) {
        return recepcionMensajeRepository.findByEventoCatchId(eventoCatchId);
    }

    // ===================== Seguridad y Validación =====================

    /**
     * Valida el token/firma del emisor externo contra los conectores 
     * configurados en la empresa destino.
     */
    private boolean validarCredencialExterna(String token, Long empresaId) {
        if (token == null || token.isBlank()) {
            return false;
        }

        // Buscar conectores activos de la empresa y verificar si alguno coincide con el token
        List<ConectorExterno> conectores = conectorExternoRepository.findByEmpresaIdAndActivo(empresaId, true);

        for (ConectorExterno conector : conectores) {
            if (conector.getCredencialRef() != null && conector.getCredencialRef().equals(token)) {
                log.info("SEGURIDAD: Token externo validado exitosamente contra conector '{}'", conector.getNombre());
                return true;
            }
        }

        return false;
    }

    /**
     * Sanitiza el payload JSON para evitar inyección de datos maliciosos.
     * En producción, este método debería usar una librería de sanitización robusta.
     */
    private String sanitizarPayload(String payload) {
        if (payload == null) return null;

        // Eliminar caracteres potencialmente peligrosos en contexto de inyección
        return payload
                .replace("<script>", "")
                .replace("</script>", "")
                .replace("${", "{")   // Prevenir JNDI/Expression injection
                .replace("#{", "{");  // Prevenir EL injection
    }

    /**
     * Mapea las variables del payload recibido al schema esperado por el CATCH.
     * En una implementación completa, esto haría un merge inteligente de campos JSON.
     */
    private String mapearVariables(String payloadRecibido, String schemaEsperado) {
        if (payloadRecibido == null) return null;
        if (schemaEsperado == null) return payloadRecibido;

        // NOTA: En producción, implementar mapeo real campo-a-campo usando Jackson ObjectMapper
        return "{\"payload_original\": " + payloadRecibido + ", \"schema_aplicado\": \"" + schemaEsperado + "\"}";
    }
}
