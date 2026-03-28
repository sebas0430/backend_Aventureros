package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.RecepcionMensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCatchService implements IMessageCatchService {

    private final RecepcionMensajeRepository recepcionMensajeRepository;
    private final @Lazy IEventoMensajeService eventoMensajeService;
    private final @Lazy IEmpresaService empresaService;
    private final @Lazy INotificacionExternaService notificacionExternaService;
    private final @Lazy IInstanciaProcesoService instanciaProcesoService;

    @Override
    @Transactional
    public List<RecepcionMensaje> recibirMensaje(MensajeCatchDTO dto) {
        Empresa empresa = empresaService.obtenerEmpresaPorId(dto.getEmpresaId());

        String payloadSanitizado = sanitizarPayload(dto.getPayload());

        boolean credencialValidada = true;
        if ("EXTERNO".equalsIgnoreCase(dto.getFuente())) {
            credencialValidada = notificacionExternaService.validarCredencialExterna(dto.getTokenSeguridad(),
                    empresa.getId());
            if (!credencialValidada) {
                log.warn(
                        "SEGURIDAD: Intento de Message Catch RECHAZADO. Emisor='{}', Mensaje='{}', Empresa={}. Token inválido.",
                        dto.getIdentificadorEmisor(), dto.getNombreMensaje(), empresa.getId());

                throw new BusinessRuleException(
                        "Credencial/token de seguridad inválido. El mensaje externo ha sido rechazado. " +
                                "Emisor: " + dto.getIdentificadorEmisor());
            }
        }

        List<EventoMensaje> catchesActivos;
        if (dto.getCorrelationKey() != null && !dto.getCorrelationKey().isBlank()) {
            catchesActivos = eventoMensajeService.buscarCatchesActivosPorNombreYCorrelationYEmpresa(
                    dto.getNombreMensaje(), dto.getCorrelationKey(), empresa.getId());
        } else {
            catchesActivos = eventoMensajeService.buscarCatchesActivosPorNombreYEmpresa(
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

            List<InstanciaProceso> instanciasActivas;
            if (dto.getCorrelationKey() != null && catchEvento.getTipoCorrelacion() == ReglaCorrelacion.BUSINESS_KEY) {
                instanciasActivas = instanciaProcesoService.listarActivasPorBusinessKeyYProceso(
                        dto.getCorrelationKey(), catchEvento.getProceso().getId());
            } else {
                instanciasActivas = instanciaProcesoService.listarActivasPorProceso(
                        catchEvento.getProceso().getId());
            }

            List<InstanciaProceso> destinos = new ArrayList<>();

            if (instanciasActivas.isEmpty()) {
                if (catchEvento.isCrearInstanciaSiFalla()) {
                    InstanciaProceso nuevaInstancia = InstanciaProceso.builder()
                            .proceso(catchEvento.getProceso())
                            .businessKey(dto.getCorrelationKey())
                            .estado(EstadoInstancia.ACTIVA)
                            .variables(variablesMapeadas)
                            .build();
                    destinos.add(instanciaProcesoService.guardarInstancia(nuevaInstancia));
                } else {
                    log.warn(
                            "Correlación fallida: No se encontraron instancias para el Catch en el proceso {} y 'crearInstanciaSiFalla' es falso",
                            catchEvento.getProceso().getId());
                    continue; // Skip este catch
                }
            } else if (instanciasActivas.size() == 1) {
                destinos.add(instanciasActivas.get(0));
            } else {
                PoliticaMultiplesCoincidencias politica = catchEvento.getPoliticaMultiples() != null
                        ? catchEvento.getPoliticaMultiples()
                        : PoliticaMultiplesCoincidencias.ERROR;

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
                        destinos.add(instanciaProcesoService.guardarInstancia(nueva));
                    }
                    case ERROR -> throw new BusinessRuleException(
                            "Se encontraron múltiples instancias activas y la política de correlación indica ERROR para el proceso "
                                    + catchEvento.getProceso().getId());
                }
            }

            for (InstanciaProceso in : destinos) {
                if (in.getId() != null) {
                    in.setVariables(variablesMapeadas);
                    instanciaProcesoService.guardarInstancia(in);
                }

                String accion = (in.getId() != null && !catchEvento.isCrearInstanciaSiFalla()
                        && !instanciasActivas.isEmpty())
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
                        .detalle("Entregado a Instancia " + in.getId() + " del Proceso "
                                + catchEvento.getProceso().getId())
                        .build();

                recepciones.add(recepcionMensajeRepository.save(recepcion));

                log.info(
                        "AUDITORIA (MESSAGE CATCH): Instancia {} (Proceso {}) recibió mensaje '{}' desde {}. Acción: {}",
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

    private String sanitizarPayload(String payload) {
        if (payload == null)
            return null;
        return payload
                .replace("<script>", "")
                .replace("</script>", "")
                .replace("${", "{")
                .replace("#{", "{");
    }

    private String mapearVariables(String payloadRecibido, String schemaEsperado) {
        if (payloadRecibido == null)
            return null;
        if (schemaEsperado == null)
            return payloadRecibido;
        return "{\"payload_original\": " + payloadRecibido + ", \"schema_aplicado\": \"" + schemaEsperado + "\"}";
    }
}
