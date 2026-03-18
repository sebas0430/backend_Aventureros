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
            // Mapear variables del payload al schema esperado por el CATCH
            String variablesMapeadas = mapearVariables(payloadSanitizado, catchEvento.getPayloadSchema());

            // Determinar acción: si el proceso está en BORRADOR/EN_REVISION se "activa", si está PUBLICADO se "continúa"
            EstadoProceso estadoProceso = catchEvento.getProceso().getEstado();
            String accion = estadoProceso == EstadoProceso.PUBLICADO 
                    ? "CONTINUAR_INSTANCIA" 
                    : "ACTIVAR_INSTANCIA";

            RecepcionMensaje recepcion = RecepcionMensaje.builder()
                    .eventoCatch(catchEvento)
                    .fuenteMensaje(dto.getFuente().toUpperCase())
                    .identificadorEmisor(dto.getIdentificadorEmisor())
                    .payloadRecibido(payloadSanitizado)
                    .variablesMapeadas(variablesMapeadas)
                    .correlationKeyUsada(dto.getCorrelationKey())
                    .credencialValidada(credencialValidada)
                    .accionTomada(accion)
                    .detalle("Mensaje '" + dto.getNombreMensaje() + "' recibido exitosamente por el proceso " 
                            + catchEvento.getProceso().getId())
                    .build();

            recepciones.add(recepcionMensajeRepository.save(recepcion));

            log.info("AUDITORIA (MESSAGE CATCH): Proceso {} recibió mensaje '{}' desde {} (emisor={}). Acción: {}",
                    catchEvento.getProceso().getId(), dto.getNombreMensaje(), dto.getFuente(),
                    dto.getIdentificadorEmisor(), accion);
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

        // TODO: En producción, implementar mapeo real campo-a-campo usando Jackson ObjectMapper
        return "{\"payload_original\": " + payloadRecibido + ", \"schema_aplicado\": \"" + schemaEsperado + "\"}";
    }
}
