package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionExternaService implements INotificacionExternaService {

    private final ConectorExternoRepository conectorExternoRepository;
    private final NotificacionExternaRepository notificacionExternaRepository;
    private final @Lazy IEmpresaService empresaService;
    private final @Lazy IProcesoService procesoService;
    private final @Lazy IUsuarioService usuarioService;

    @Override
    @Transactional
    public ConectorExterno crearConector(ConectorExternoRegistroDTO dto) {
        Empresa empresa = empresaService.obtenerEmpresaPorId(dto.getEmpresaId());

        validarAdminEmpresa(dto.getUsuarioId(), empresa.getId());

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

        ConectorExterno guardado = conectorExternoRepository.save(conector);
        log.info("AUDITORIA: Usuario {} creó conector externo '{}' (tipo={}) para empresa {}",
                dto.getUsuarioId(), guardado.getNombre(), guardado.getTipo(), empresa.getId());
        return guardado;
    }

    @Override
    @Transactional
    public ConectorExterno editarConector(Long id, ConectorExternoRegistroDTO dto) {
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

        log.info("AUDITORIA: Usuario {} editó conector externo ID={}", dto.getUsuarioId(), id);
        return conectorExternoRepository.save(conector);
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
    public List<ConectorExterno> listarConectoresPorEmpresa(Long empresaId) {
        return conectorExternoRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarCredencialExterna(String token, Long empresaId) {
        if (token == null || token.isBlank()) {
            return false;
        }

        List<ConectorExterno> conectores = conectorExternoRepository.findByEmpresaIdAndActivo(empresaId, true);

        for (ConectorExterno conector : conectores) {
            if (conector.getCredencialRef() != null && conector.getCredencialRef().equals(token)) {
                log.info("SEGURIDAD: Token externo validado exitosamente contra conector '{}'", conector.getNombre());
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public NotificacionExterna enviarMensajeExterno(EnvioExternoDTO dto) {
        ConectorExterno conector = conectorExternoRepository.findById(dto.getConectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conector externo no encontrado"));

        Proceso proceso = procesoService.obtenerProcesoPorId(dto.getProcesoId());

        Usuario usuario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioId());

        if (!conector.getEmpresa().getId().equals(proceso.getEmpresa().getId())) {
            throw new BusinessRuleException("El conector y el proceso no pertenecen a la misma empresa.");
        }

        if (!conector.isActivo()) {
            throw new BusinessRuleException("El conector está deshabilitado.");
        }

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

        return notificacionExternaRepository.save(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionExterna> listarLogsPorProceso(Long procesoId) {
        return notificacionExternaRepository.findByProcesoId(procesoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionExterna> listarLogsPorConector(Long conectorId) {
        return notificacionExternaRepository.findByConectorId(conectorId);
    }

    private String despacharAlAdaptador(ConectorExterno conector, String payload) {
        return switch (conector.getTipo()) {
            case EMAIL -> adaptadorEmail(conector, payload);
            case WEBHOOK -> adaptadorWebhook(conector, payload);
            case QUEUE -> adaptadorQueue(conector, payload);
        };
    }

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

    private void validarAdminEmpresa(Long usuarioId, Long empresaId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol()) || !usuario.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessRuleException("Solo un administrador de la empresa puede gestionar conectores externos.");
        }
    }
}
