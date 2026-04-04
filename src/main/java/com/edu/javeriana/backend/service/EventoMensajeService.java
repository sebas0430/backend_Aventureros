package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeEjecucionDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.EventoMensajeRepository;
import com.edu.javeriana.backend.repository.MensajeEjecucionRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventoMensajeService implements IEventoMensajeService {

    private final EventoMensajeRepository eventoMensajeRepository;
    private final MensajeEjecucionRepository mensajeEjecucionRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public EventoMensajeService(EventoMensajeRepository eventoMensajeRepository,
                                MensajeEjecucionRepository mensajeEjecucionRepository,
                                ProcesoRepository procesoRepository,
                                UsuarioRepository usuarioRepository,
                                ModelMapper modelMapper) {
        this.eventoMensajeRepository   = eventoMensajeRepository;
        this.mensajeEjecucionRepository = mensajeEjecucionRepository;
        this.procesoRepository          = procesoRepository;
        this.usuarioRepository          = usuarioRepository;
        this.modelMapper                = modelMapper;
    }

    @Override
    @Transactional
    public EventoMensajeRegistroDTO crearEvento(EventoMensajeRegistroDTO dto) {
        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

                Usuario usuario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioId());

                if (!usuario.getEmpresa().getId().equals(proceso.getEmpresa().getId())) {
                        throw new BusinessRuleException("No perteneces a la empresa de este proceso.");
                }

                EventoMensaje evento = EventoMensaje.builder()
                                .nombreMensaje(dto.getNombreMensaje())
                                .tipo(dto.getTipo())
                                .payloadSchema(dto.getPayloadSchema())
                                .fallback(dto.getFallback())
                                .proceso(proceso)
                                .build();

        EventoMensaje guardado = eventoMensajeRepository.save(evento);
        log.info("AUDITORIA: Usuario {} creó evento de mensaje '{}' ({}) en el proceso {}",
                usuario.getId(), guardado.getNombreMensaje(), guardado.getTipo(), proceso.getId());

        EventoMensajeRegistroDTO response = modelMapper.map(guardado, EventoMensajeRegistroDTO.class);
        response.setProcesoId(guardado.getProceso().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoMensajeRegistroDTO> listarPorProceso(Long procesoId) {
        return eventoMensajeRepository.findByProcesoId(procesoId)
                .stream()
                .map(e -> {
                    EventoMensajeRegistroDTO dto = modelMapper.map(e, EventoMensajeRegistroDTO.class);
                    dto.setProcesoId(e.getProceso().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarEvento(Long eventoId, Long usuarioId) {
        EventoMensaje evento = eventoMensajeRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmpresa().getId().equals(evento.getProceso().getEmpresa().getId())) {
            throw new BusinessRuleException("No perteneces a la empresa de este proceso.");
        }

                eventoMensajeRepository.delete(evento);
                log.info("AUDITORIA: Usuario {} eliminó el evento de mensaje ID={}", usuarioId, eventoId);
        }

    @Override
    @Transactional
    public List<MensajeEjecucionDTO> lanzarMensaje(MensajeLanzarDTO dto) {
        EventoMensaje origen = eventoMensajeRepository.findById(dto.getEventoOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento de origen no encontrado"));

                Usuario usuario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioId());

                if (origen.getTipo() != TipoEventoMensaje.THROW) {
                        throw new BusinessRuleException(
                                        "El evento de origen debe ser de tipo THROW para poder lanzarlo.");
                }

        List<EventoMensaje> destinosPosibles = eventoMensajeRepository
                .findByNombreMensajeAndTipoAndEmpresaIdAndEstado(
                        origen.getNombreMensaje(),
                        TipoEventoMensaje.CATCH,
                        origen.getProceso().getEmpresa().getId(),
                        EstadoProceso.PUBLICADO
                );

                List<MensajeEjecucion> ejecucionesGuardadas = new ArrayList<>();

        if (destinosPosibles.isEmpty()) {
            ComportamientoFallback comportamiento = origen.getFallback();
            if (comportamiento == null) comportamiento = ComportamientoFallback.ERROR;

            log.warn("AUDITORIA: Usuario {} falló al lanzar mensaje '{}'. Razón: No hay receptores (CATCH) activos.",
                    usuario.getId(), origen.getNombreMensaje());

            if (comportamiento == ComportamientoFallback.ERROR) {
                throw new BusinessRuleException(
                        "No se encontró ningún receptor (Catch) para este mensaje y la configuración indica abortar por ERROR.");
            }

            MensajeEjecucion fallaLog = MensajeEjecucion.builder()
                    .eventoOrigen(origen)
                    .eventoDestino(null)
                    .payload(dto.getPayload())
                    .estado(EstadoMensaje.ERROR_SIN_RECEPTOR)
                    .detalleError("Lanzado por usuario " + usuario.getId() +
                            ". No receptor para mensaje: " + origen.getNombreMensaje() +
                            ". Estrategia: " + comportamiento.name())
                    .build();

            return List.of(toEjecucionDTO(mensajeEjecucionRepository.save(fallaLog)));
        }

        for (EventoMensaje destino : destinosPosibles) {
            MensajeEjecucion ejecucion = MensajeEjecucion.builder()
                    .eventoOrigen(origen)
                    .eventoDestino(destino)
                    .payload(dto.getPayload())
                    .estado(EstadoMensaje.PROCESADO)
                    .build();

            ejecucionesGuardadas.add(mensajeEjecucionRepository.save(ejecucion));

            log.info("AUDITORIA (MESSAGE THROW): Usuario {} emitió el mensaje '{}' correctamente " +
                            "desde el proceso {} hacia el proceso receptor {}",
                    usuario.getId(), origen.getNombreMensaje(),
                    origen.getProceso().getId(), destino.getProceso().getId());
        }

        return ejecucionesGuardadas.stream()
                .map(this::toEjecucionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeEjecucionDTO> listarHistorialPorEventoOrigen(Long eventoOrigenId) {
        return mensajeEjecucionRepository.findByEventoOrigenId(eventoOrigenId)
                .stream()
                .map(this::toEjecucionDTO)
                .collect(Collectors.toList());
    }

    // ===================== Helpers =====================

    private MensajeEjecucionDTO toEjecucionDTO(MensajeEjecucion e) {
        MensajeEjecucionDTO dto = modelMapper.map(e, MensajeEjecucionDTO.class);
        dto.setEventoOrigenId(e.getEventoOrigen().getId());
        if (e.getEventoDestino() != null) {
            dto.setEventoDestinoId(e.getEventoDestino().getId());
        }
        return dto;
    }
}