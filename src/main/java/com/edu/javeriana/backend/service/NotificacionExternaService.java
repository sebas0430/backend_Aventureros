package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.INotificacionExternaService;
import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.dto.NotificacionExternaDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class NotificacionExternaService implements INotificacionExternaService {

    private final ConectorExternoRepository conectorExternoRepository;
    private final NotificacionExternaRepository notificacionExternaRepository;
    private final IEmpresaService empresaService;
    private final IProcesoService procesoService;
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public NotificacionExternaService(ConectorExternoRepository conectorExternoRepository,
            NotificacionExternaRepository notificacionExternaRepository,
            @Lazy IEmpresaService empresaService,
            @Lazy IProcesoService procesoService,
            @Lazy IUsuarioService usuarioService,
            ModelMapper modelMapper) {
        this.conectorExternoRepository = conectorExternoRepository;
        this.notificacionExternaRepository = notificacionExternaRepository;
        this.empresaService    = empresaService;
        this.procesoService    = procesoService;
        this.usuarioService    = usuarioService;
        this.modelMapper = modelMapper;
    }

    // ===================== Gestión de Conectores =====================

    @Override
    @Transactional
    public ConectorExternoRegistroDTO crearConector(ConectorExternoRegistroDTO dto) {
        // Buscamos la empresa que quiere conectarse con algo de afuera (ej. un CRM).
        Empresa empresa = empresaService.obtenerEmpresaEntity(dto.getEmpresaId());

        // Validamos que sea el administrador de la empresa el que crea esto.
        validarAdminEmpresa(dto.getUsuarioId(), empresa.getId());

        // Configuramos la puerta de salida (Webhook, Email, etc.).
        ConectorExterno conector = ConectorExterno.builder()
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .destino(dto.getDestino())
                .puerto(dto.getPuerto())
                .credencialRef(dto.getCredencialRef())
                .usuarioAuth(dto.getUsuarioAuth())
                .headersJson(dto.getHeadersJson())
                .maxReintentos(dto.getMaxReintentos() > 0 ? dto.getMaxReintentos() : 3)
                .empresa(empresa)
                .build();

        // Lo guardamos en la base de datos.
        ConectorExterno guardado = conectorExternoRepository.save(conector);
        log.info("AUDITORIA: Usuario {} creó conector externo '{}' (tipo={}) para empresa {}",
                dto.getUsuarioId(), guardado.getNombre(), guardado.getTipo(), empresa.getId());

        // Devolvemos el DTO con la configuración lista.
        ConectorExternoRegistroDTO response = modelMapper.map(guardado, ConectorExternoRegistroDTO.class);
        response.setEmpresaId(guardado.getEmpresa().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public ConectorExternoRegistroDTO editarConector(Long id, ConectorExternoRegistroDTO dto) {
        ConectorExterno conector = conectorExternoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conector no encontrado"));

        validarAdminEmpresa(dto.getUsuarioId(), conector.getEmpresa().getId());

        conector.setNombre(dto.getNombre());
        conector.setTipo(dto.getTipo());
        conector.setDestino(dto.getDestino());
        conector.setPuerto(dto.getPuerto());
        conector.setCredencialRef(dto.getCredencialRef());
        conector.setUsuarioAuth(dto.getUsuarioAuth());
        conector.setHeadersJson(dto.getHeadersJson());
        if (dto.getMaxReintentos() > 0)
            conector.setMaxReintentos(dto.getMaxReintentos());

        ConectorExterno actualizado = conectorExternoRepository.save(conector);
        log.info("AUDITORIA: Usuario {} editó conector externo ID={}", dto.getUsuarioId(), id);

        ConectorExternoRegistroDTO response = modelMapper.map(actualizado, ConectorExternoRegistroDTO.class);
        response.setEmpresaId(actualizado.getEmpresa().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarConector(Long id, Long usuarioId) {
        ConectorExterno conector = conectorExternoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conector no encontrado"));

        validarAdminEmpresa(usuarioId, conector.getEmpresa().getId());

        conectorExternoRepository.delete(conector);
        log.info("AUDITORIA: Usuario {} eliminó conector externo ID={}", usuarioId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConectorExternoRegistroDTO> listarConectoresPorEmpresa(Long empresaId) {
        return conectorExternoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(c -> {
                    ConectorExternoRegistroDTO dto = modelMapper.map(c, ConectorExternoRegistroDTO.class);
                    dto.setEmpresaId(c.getEmpresa().getId());
                    return dto;
                })
                .toList();
    }

    // ===================== Envío de Mensajes =====================

    @Override
    @Transactional
    public NotificacionExternaDTO enviarMensajeExterno(EnvioExternoDTO dto) {
        ConectorExterno conector = conectorExternoRepository.findById(dto.getConectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conector externo no encontrado"));

        Proceso proceso = procesoService.obtenerProcesoEntity(dto.getProcesoId());

        Usuario usuario = usuarioService.obtenerUsuarioEntity(dto.getUsuarioId());

        if (!conector.getEmpresa().getId().equals(proceso.getEmpresa().getId())) {
            throw new BusinessRuleException("El conector y el proceso no pertenecen a la misma empresa.");
        }

        if (!conector.isActivo()) {
            throw new BusinessRuleException("El conector está deshabilitado.");
        }

        // Simular/delegar el envío al adaptador correspondiente
        NotificacionExterna notificacion = NotificacionExterna.builder()
                .conector(conector)
                .proceso(proceso)
                .payload(dto.getPayload())
                .estado(EstadoEnvioExterno.PENDIENTE)
                .build();

        notificacion = notificacionExternaRepository.save(notificacion);

        try {
            String respuesta = despacharAlAdaptador(conector, dto.getPayload());
            notificacion.setEstado(EstadoEnvioExterno.ENVIADO);
            notificacion.setRespuestaExterna(respuesta);
            notificacion.setIntentosRealizados(1);

            log.info("AUDITORIA: Usuario {} envió notificación externa via {} al destino '{}' desde el proceso {}",
                    usuario.getId(), conector.getTipo(), conector.getDestino(), proceso.getId());

        } catch (Exception e) {
            notificacion.setIntentosRealizados(notificacion.getIntentosRealizados() + 1);

            if (notificacion.getIntentosRealizados() < conector.getMaxReintentos()) {
                notificacion.setEstado(EstadoEnvioExterno.REINTENTANDO);
                notificacion.setDetalleError(
                        "Intento " + notificacion.getIntentosRealizados() + " fallido: " + e.getMessage());
                log.warn("AUDITORIA: Notificación ID={} falló, reintentando ({}/{})",
                        notificacion.getId(), notificacion.getIntentosRealizados(), conector.getMaxReintentos());
            } else {
                notificacion.setEstado(EstadoEnvioExterno.ERROR);
                notificacion.setDetalleError("Todos los reintentos agotados. Último error: " + e.getMessage());
                log.error("AUDITORIA: Notificación ID={} falló definitivamente tras {} intentos",
                        notificacion.getId(), conector.getMaxReintentos());
            }
        }

        NotificacionExterna guardada = notificacionExternaRepository.save(notificacion);
        return toNotificacionDTO(guardada);
    }

    // ===================== Logs =====================

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionExternaDTO> listarLogsPorProceso(Long procesoId) {
        return notificacionExternaRepository.findByProcesoId(procesoId)
                .stream()
                .map(this::toNotificacionDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionExternaDTO> listarLogsPorConector(Long conectorId) {
        return notificacionExternaRepository.findByConectorId(conectorId)
                .stream()
                .map(this::toNotificacionDTO)
                .toList();
    }

    // ===================== Adaptadores =====================

    private String despacharAlAdaptador(ConectorExterno conector, String payload) {
        return switch (conector.getTipo()) {
            case EMAIL -> adaptadorEmail(conector, payload);
            case WEBHOOK -> adaptadorWebhook(conector, payload);
            case QUEUE -> adaptadorQueue(conector, payload);
        };
    }

    /** Adaptador EMAIL: simula envío SMTP */
    private String adaptadorEmail(ConectorExterno conector, String payload) {
        log.info("[ADAPTADOR EMAIL] Enviando a host={}, puerto={}, usuario={}, payload={}",
                conector.getDestino(), conector.getPuerto(), conector.getUsuarioAuth(), payload);
        return "EMAIL_OK: Simulado exitosamente hacia " + conector.getDestino();
    }

    private String adaptadorWebhook(ConectorExterno conector, String payload) {
        log.info("[ADAPTADOR WEBHOOK] POST a URL={}, headers={}, payload={}",
                conector.getDestino(), conector.getHeadersJson(), payload);
        return "WEBHOOK_OK: Simulado exitosamente hacia " + conector.getDestino();
    }

    private String adaptadorQueue(ConectorExterno conector, String payload) {
        log.info("[ADAPTADOR QUEUE] Publicando en broker={}, puerto={}, payload={}",
                conector.getDestino(), conector.getPuerto(), payload);
        return "QUEUE_OK: Simulado exitosamente hacia " + conector.getDestino();
    }

    // ===================== Helpers =====================

    private NotificacionExternaDTO toNotificacionDTO(NotificacionExterna n) {
        NotificacionExternaDTO dto = modelMapper.map(n, NotificacionExternaDTO.class);
        dto.setConectorId(n.getConector().getId());
        dto.setProcesoId(n.getProceso().getId());
        return dto;
    }

    private void validarAdminEmpresa(Long usuarioId, Long empresaId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol()) || !usuario.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessRuleException("Solo un administrador de la empresa puede gestionar conectores externos.");
        }
    }
}