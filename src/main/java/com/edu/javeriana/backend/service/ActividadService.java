package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActividadService implements IActividadService {

    private final ActividadRepository actividadRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    // ─────────────────────────────────────────────
    // HU-08: Crear actividad
    // ─────────────────────────────────────────────
    @Transactional
    @Override
    public Actividad crearActividad(ActividadRegistroDTO dto) {

        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Solo EDITOR o ADMINISTRADOR_EMPRESA pueden crear actividades
        boolean autorizado = RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(usuario.getRol())
                || RolGlobal.EDITOR.name().equals(usuario.getRol());

        if (!autorizado) {
            throw new BusinessRuleException(
                    "No tienes permisos para agregar actividades. Se requiere rol EDITOR o ADMINISTRADOR_EMPRESA.");
        }

        Actividad actividad = Actividad.builder()
                .nombre(dto.getNombre())
                .tipoActividad(dto.getTipoActividad())
                .descripcion(dto.getDescripcion())
                .rolResponsable(dto.getRolResponsable())
                .orden(dto.getOrden())
                .proceso(proceso)
                .activa(true)
                .build();

        actividad = actividadRepository.save(actividad);
        log.info("Actividad {} creada exitosamente en proceso {}", actividad.getId(), proceso.getId());

        // Guardar en historial del proceso (HU-08: la actividad queda vinculada)
        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso)
                .usuario(usuario)
                .accion("CREACION_ACTIVIDAD")
                .detalle("Se agregó la actividad '" + actividad.getNombre()
                        + "' de tipo '" + actividad.getTipoActividad()
                        + "' con rol responsable '" + actividad.getRolResponsable() + "'.")
                .build();
        historialProcesoRepository.save(historial);

        return actividad;
    }

    // ─────────────────────────────────────────────
    // HU-09: Editar actividad
    // ─────────────────────────────────────────────
    @Transactional
    @Override
    public Actividad editarActividad(Long actividadId, ActividadEdicionDTO dto) {

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Solo EDITOR o ADMINISTRADOR_EMPRESA pueden editar
        boolean autorizado = RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(usuario.getRol())
                || RolGlobal.EDITOR.name().equals(usuario.getRol());

        if (!autorizado) {
            throw new BusinessRuleException(
                    "No tienes permisos para editar actividades. Se requiere rol EDITOR o ADMINISTRADOR_EMPRESA.");
        }

        // Construir detalle de cambios para el historial (HU-09)
        StringBuilder cambios = new StringBuilder();

        if (!actividad.getNombre().equals(dto.getNombre())) {
            cambios.append("Nombre cambiado de '").append(actividad.getNombre())
                    .append("' a '").append(dto.getNombre()).append("'. ");
            actividad.setNombre(dto.getNombre());
        }
        if (!actividad.getTipoActividad().equals(dto.getTipoActividad())) {
            cambios.append("Tipo cambiado de '").append(actividad.getTipoActividad())
                    .append("' a '").append(dto.getTipoActividad()).append("'. ");
            actividad.setTipoActividad(dto.getTipoActividad());
        }
        if (!actividad.getDescripcion().equals(dto.getDescripcion())) {
            cambios.append("Descripción actualizada. ");
            actividad.setDescripcion(dto.getDescripcion());
        }
        if (!actividad.getRolResponsable().equals(dto.getRolResponsable())) {
            cambios.append("Rol responsable cambiado de '").append(actividad.getRolResponsable())
                    .append("' a '").append(dto.getRolResponsable()).append("'. ");
            actividad.setRolResponsable(dto.getRolResponsable());
        }
        if (dto.getOrden() != null && !dto.getOrden().equals(actividad.getOrden())) {
            cambios.append("Orden cambiado de ").append(actividad.getOrden())
                    .append(" a ").append(dto.getOrden()).append(". ");
            actividad.setOrden(dto.getOrden());
        }

        if (!cambios.isEmpty()) {
            actividad = actividadRepository.save(actividad);
            log.info("Actividad {} editada exitosamente", actividadId);

            // HU-09: Los cambios se guardan en el historial del proceso
            HistorialProceso historial = HistorialProceso.builder()
                    .proceso(actividad.getProceso())
                    .usuario(usuario)
                    .accion("EDICION_ACTIVIDAD")
                    .detalle("Actividad '" + actividad.getNombre() + "': " + cambios.toString().trim())
                    .build();
            historialProcesoRepository.save(historial);
        }

        return actividad;
    }

    // ─────────────────────────────────────────────
    // HU-10: Eliminar actividad
    // ─────────────────────────────────────────────
    @Transactional
    @Override
    public void eliminarActividad(Long actividadId, Long usuarioId) {

        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // HU-10: Solo el ADMINISTRADOR_EMPRESA puede eliminar
        if (!RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador puede eliminar actividades.");
        }

        // Marcar como inactiva (soft delete — la confirmación viene del frontend)
        actividad.setActiva(false);
        actividadRepository.save(actividad);
        log.info("Actividad {} marcada como inactiva (soft delete)", actividadId);

        // HU-10: El flujo del proceso se ajusta automáticamente —
        // reordenar las actividades restantes del proceso
        List<Actividad> actividadesActivas = actividadRepository
                .findByProcesoIdAndActivaTrueOrderByOrdenAsc(actividad.getProceso().getId());

        for (int i = 0; i < actividadesActivas.size(); i++) {
            actividadesActivas.get(i).setOrden(i + 1);
        }
        actividadRepository.saveAll(actividadesActivas);

        // Guardar en historial del proceso
        HistorialProceso historial = HistorialProceso.builder()
                .proceso(actividad.getProceso())
                .usuario(usuario)
                .accion("ELIMINACION_ACTIVIDAD")
                .detalle("La actividad '" + actividad.getNombre()
                        + "' fue eliminada. El flujo del proceso fue reajustado.")
                .build();
        historialProcesoRepository.save(historial);
    }

    // ─────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    @Override
    public List<Actividad> listarPorProceso(Long procesoId) {
        return actividadRepository.findByProcesoIdAndActivaTrueOrderByOrdenAsc(procesoId);
    }

    @Transactional(readOnly = true)
    @Override
    public Actividad obtenerPorId(Long actividadId) {
        return actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
    }
}