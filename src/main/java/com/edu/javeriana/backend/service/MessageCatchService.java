package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IMessageCatchService;
import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class MessageCatchService implements IMessageCatchService {

    private final EventoMensajeRepository eventoMensajeRepository;
    private final RecepcionMensajeRepository recepcionMensajeRepository;
    private final ConectorExternoRepository conectorExternoRepository;
    private final EmpresaRepository empresaRepository;
    private final InstanciaProcesoRepository instanciaProcesoRepository;
    private final ModelMapper modelMapper;

    public MessageCatchService(EventoMensajeRepository eventoMensajeRepository,
                               RecepcionMensajeRepository recepcionMensajeRepository,
                               ConectorExternoRepository conectorExternoRepository,
                               EmpresaRepository empresaRepository,
                               InstanciaProcesoRepository instanciaProcesoRepository,
                               ModelMapper modelMapper) {
        this.eventoMensajeRepository    = eventoMensajeRepository;
        this.recepcionMensajeRepository = recepcionMensajeRepository;
        this.conectorExternoRepository  = conectorExternoRepository;
        this.empresaRepository          = empresaRepository;
        this.instanciaProcesoRepository = instanciaProcesoRepository;
        this.modelMapper                = modelMapper;
    }

    @Override
    @Transactional
    public List<MensajeCatchDTO> recibirMensaje(MensajeCatchDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa destino no encontrada"));

        String payloadSanitizado = sanitizarPayload(dto.getPayload());
        boolean credencialValidada = validarSeguridad(dto, empresa.getId());

        List<EventoMensaje> catchesActivos = buscarCatchesActivos(dto, empresa.getId());
        if (catchesActivos.isEmpty()) {
            throw new BusinessRuleException("No hay procesos con un CATCH activo esperando el mensaje '" + dto.getNombreMensaje() + "'.");
        }

        List<RecepcionMensaje> recepciones = new ArrayList<>();
        for (EventoMensaje catchEvento : catchesActivos) {
            String variablesMapeadas = mapearVariables(payloadSanitizado, catchEvento.getPayloadSchema());
            List<InstanciaProceso> destinos = determinarInstanciasDestino(catchEvento, dto, variablesMapeadas);
            recepciones.addAll(procesarEntregaMensaje(catchEvento, destinos, dto, payloadSanitizado, variablesMapeadas, credencialValidada));
        }

        return recepciones.stream()
                .map(r -> modelMapper.map(dto, MensajeCatchDTO.class))
                .toList();
    }

    private boolean validarSeguridad(MensajeCatchDTO dto, Long empresaId) {
        if (!"EXTERNO".equalsIgnoreCase(dto.getFuente())) {
            return true;
        }
        boolean credencialValidada = validarCredencialExterna(dto.getTokenSeguridad(), empresaId);
        if (!credencialValidada) {
            log.warn("SEGURIDAD: Intento de Message Catch RECHAZADO. Emisor='{}', Mensaje='{}', Empresa={}. Token inválido.",
                    dto.getIdentificadorEmisor(), dto.getNombreMensaje(), empresaId);
            throw new BusinessRuleException(
                    "Credencial/token de seguridad inválido. El mensaje externo ha sido rechazado. Emisor: " + dto.getIdentificadorEmisor());
        }
        return true;
    }

    private List<EventoMensaje> buscarCatchesActivos(MensajeCatchDTO dto, Long empresaId) {
        if (dto.getCorrelationKey() != null && !dto.getCorrelationKey().isBlank()) {
            return eventoMensajeRepository.findActiveCatchesByNombreAndCorrelationAndEmpresa(
                    dto.getNombreMensaje(), dto.getCorrelationKey(), empresaId);
        }
        return eventoMensajeRepository.findActiveCatchesByNombreAndEmpresa(
                dto.getNombreMensaje(), empresaId);
    }

    private List<InstanciaProceso> determinarInstanciasDestino(EventoMensaje catchEvento, MensajeCatchDTO dto, String variablesMapeadas) {
        List<InstanciaProceso> instanciasActivas;
        if (dto.getCorrelationKey() != null && catchEvento.getTipoCorrelacion() == ReglaCorrelacion.BUSINESS_KEY) {
            instanciasActivas = instanciaProcesoRepository.findByBusinessKeyAndProcesoIdAndEstado(
                    dto.getCorrelationKey(), catchEvento.getProceso().getId(), EstadoInstancia.ACTIVA);
        } else {
            instanciasActivas = instanciaProcesoRepository.findByProcesoIdAndEstado(
                    catchEvento.getProceso().getId(), EstadoInstancia.ACTIVA);
        }

        if (instanciasActivas.isEmpty()) {
            return manejarSinInstanciasActivas(catchEvento, dto, variablesMapeadas);
        } else if (instanciasActivas.size() == 1) {
            return List.of(instanciasActivas.get(0));
        } else {
            return aplicarPoliticaMultiples(catchEvento, dto, variablesMapeadas, instanciasActivas);
        }
    }

    private List<InstanciaProceso> manejarSinInstanciasActivas(EventoMensaje catchEvento, MensajeCatchDTO dto, String variablesMapeadas) {
        if (catchEvento.isCrearInstanciaSiFalla()) {
            InstanciaProceso nuevaInstancia = InstanciaProceso.builder()
                    .proceso(catchEvento.getProceso()).businessKey(dto.getCorrelationKey())
                    .estado(EstadoInstancia.ACTIVA).variables(variablesMapeadas).build();
            return List.of(instanciaProcesoRepository.save(nuevaInstancia));
        }
        return new ArrayList<>();
    }

    private List<InstanciaProceso> aplicarPoliticaMultiples(EventoMensaje catchEvento, MensajeCatchDTO dto, String variablesMapeadas, List<InstanciaProceso> instanciasActivas) {
        PoliticaMultiplesCoincidencias politica = catchEvento.getPoliticaMultiples() != null
                ? catchEvento.getPoliticaMultiples() : PoliticaMultiplesCoincidencias.ERROR;
        
        switch (politica) {
            case ENTREGAR_A_PRIMERA -> { return List.of(instanciasActivas.get(0)); }
            case ENTREGAR_A_TODAS -> { return instanciasActivas; }
            case CREAR_NUEVA_INSTANCIA -> {
                InstanciaProceso nueva = InstanciaProceso.builder()
                        .proceso(catchEvento.getProceso()).businessKey(dto.getCorrelationKey())
                        .estado(EstadoInstancia.ACTIVA).variables(variablesMapeadas).build();
                return List.of(instanciaProcesoRepository.save(nueva));
            }
            default -> throw new BusinessRuleException(
                    "Múltiples instancias activas y la política indica ERROR para el proceso " + catchEvento.getProceso().getId());
        }
    }

    private List<RecepcionMensaje> procesarEntregaMensaje(EventoMensaje catchEvento, List<InstanciaProceso> destinos, MensajeCatchDTO dto, String payloadSanitizado, String variablesMapeadas, boolean credencialValidada) {
        List<RecepcionMensaje> recepciones = new ArrayList<>();
        for (InstanciaProceso in : destinos) {
            if (in.getId() != null) {
                in.setVariables(variablesMapeadas);
                instanciaProcesoRepository.save(in);
            }
            String accion = (in.getId() != null  && !catchEvento.isCrearInstanciaSiFalla())
                    ? "CONTINUAR_INSTANCIA_ID_" + in.getId()
                    : "ACTIVAR_NUEVA_INSTANCIA_ID_" + in.getId();

            RecepcionMensaje recepcion = RecepcionMensaje.builder()
                    .eventoCatch(catchEvento).fuenteMensaje(dto.getFuente().toUpperCase())
                    .identificadorEmisor(dto.getIdentificadorEmisor()).payloadRecibido(payloadSanitizado)
                    .variablesMapeadas(variablesMapeadas).correlationKeyUsada(dto.getCorrelationKey())
                    .credencialValidada(credencialValidada).accionTomada(accion)
                    .detalle("Entregado a Instancia " + in.getId() + " del Proceso " + catchEvento.getProceso().getId())
                    .build();
            recepciones.add(recepcionMensajeRepository.save(recepcion));

            log.info("AUDITORIA (MESSAGE CATCH): Instancia {} recibió mensaje '{}' desde {}. Acción: {}",
                    in.getId(), dto.getNombreMensaje(), dto.getFuente(), accion);
        }
        return recepciones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeCatchDTO> listarRecepcionesPorProceso(Long procesoId) {
        return recepcionMensajeRepository.findByEventoCatchProcesoId(procesoId)
                .stream()
                .map(r -> {
                    MensajeCatchDTO dto = new MensajeCatchDTO();
                    dto.setNombreMensaje(r.getEventoCatch().getNombreMensaje());
                    dto.setCorrelationKey(r.getCorrelationKeyUsada());
                    dto.setPayload(r.getPayloadRecibido());
                    dto.setFuente(r.getFuenteMensaje());
                    dto.setIdentificadorEmisor(r.getIdentificadorEmisor());
                    dto.setEmpresaId(r.getEventoCatch().getProceso().getEmpresa().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeCatchDTO> listarRecepcionesPorCatch(Long eventoCatchId) {
        return recepcionMensajeRepository.findByEventoCatchId(eventoCatchId)
                .stream()
                .map(r -> {
                    MensajeCatchDTO dto = new MensajeCatchDTO();
                    dto.setNombreMensaje(r.getEventoCatch().getNombreMensaje());
                    dto.setCorrelationKey(r.getCorrelationKeyUsada());
                    dto.setPayload(r.getPayloadRecibido());
                    dto.setFuente(r.getFuenteMensaje());
                    dto.setIdentificadorEmisor(r.getIdentificadorEmisor());
                    dto.setEmpresaId(r.getEventoCatch().getProceso().getEmpresa().getId());
                    return dto;
                })
                .toList();
    }

    private boolean validarCredencialExterna(String token, Long empresaId) {
        if (token == null || token.isBlank()) return false;
        List<ConectorExterno> conectores = conectorExternoRepository.findByEmpresaIdAndActivo(empresaId, true);
        for (ConectorExterno conector : conectores) {
            if (conector.getCredencialRef() != null && conector.getCredencialRef().equals(token)) return true;
        }
        return false;
    }

    private String sanitizarPayload(String payload) {
        if (payload == null) return null;
        return payload.replace("<script>", "").replace("</script>", "").replace("${", "{").replace("#{", "{");
    }

    private String mapearVariables(String payloadRecibido, String schemaEsperado) {
        if (payloadRecibido == null) return null;
        if (schemaEsperado == null) return payloadRecibido;
        return "{\"payload_original\": " + payloadRecibido + ", \"schema_aplicado\": \"" + schemaEsperado + "\"}";
    }
}