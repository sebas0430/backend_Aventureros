package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

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
        private final IProcesoService procesoService;
        private final IUsuarioService usuarioService;
        private final IHistorialProcesoService historialProcesoService;

        @Transactional
        @Override
        public Actividad crearActividad(ActividadRegistroDTO dto) {

                Proceso proceso = procesoService.obtenerProcesoPorId(dto.getProcesoId());
                Usuario usuario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioId());

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

                String detalle = "Se agregó la actividad '" + actividad.getNombre()
                                + "' de tipo '" + actividad.getTipoActividad()
                                + "' con rol responsable '" + actividad.getRolResponsable() + "'.";
                historialProcesoService.registrarAccion(proceso, usuario, "CREACION_ACTIVIDAD", detalle);

                return actividad;
        }

        @Transactional
        @Override
        public Actividad editarActividad(Long actividadId, ActividadEdicionDTO dto) {

                Actividad actividad = actividadRepository.findById(actividadId)
                                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
                Usuario usuario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioId());

                boolean autorizado = RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(usuario.getRol())
                                || RolGlobal.EDITOR.name().equals(usuario.getRol());

                if (!autorizado) {
                        throw new BusinessRuleException(
                                        "No tienes permisos para editar actividades. Se requiere rol EDITOR o ADMINISTRADOR_EMPRESA.");
                }

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

                        historialProcesoService.registrarAccion(
                                        actividad.getProceso(),
                                        usuario,
                                        "EDICION_ACTIVIDAD",
                                        "Actividad '" + actividad.getNombre() + "': " + cambios.toString().trim());
                }

                return actividad;
        }

        @Transactional
        @Override
        public void eliminarActividad(Long actividadId, Long usuarioId) {

                Actividad actividad = actividadRepository.findById(actividadId)
                                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

                Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

                if (!RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(usuario.getRol())) {
                        throw new BusinessRuleException(
                                        "Solo un administrador puede eliminar actividades.");
                }

                actividad.setActiva(false);
                actividadRepository.save(actividad);
                log.info("Actividad {} marcada como inactiva (soft delete)", actividadId);

                List<Actividad> actividadesActivas = actividadRepository
                                .findByProcesoIdAndActivaTrueOrderByOrdenAsc(actividad.getProceso().getId());

                for (int i = 0; i < actividadesActivas.size(); i++) {
                        actividadesActivas.get(i).setOrden(i + 1);
                }
                actividadRepository.saveAll(actividadesActivas);

                historialProcesoService.registrarAccion(
                                actividad.getProceso(),
                                usuario,
                                "ELIMINACION_ACTIVIDAD",
                                "La actividad '" + actividad.getNombre()
                                                + "' fue eliminada. El flujo del proceso fue reajustado.");
        }

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

        @Transactional(readOnly = true)
        @Override
        public boolean existeActividadPorRolProceso(Long rolProcesoId) {
                return actividadRepository.existsByRolProcesoId(rolProcesoId);
        }

        @Transactional(readOnly = true)
        @Override
        public List<Actividad> listarActividadesPorRolProceso(Long rolProcesoId) {
                return actividadRepository.findByRolProcesoId(rolProcesoId);
        }
}
