package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;

import java.util.List;

public interface IActividadService {

    // HU-08: Crear actividad vinculada a un proceso
    ActividadRegistroDTO crearActividad(ActividadRegistroDTO dto);

    // HU-09: Editar actividad (guarda cambios en historial del proceso)
    ActividadEdicionDTO editarActividad(Long actividadId, ActividadEdicionDTO dto);

    // HU-10: Eliminar actividad (solo admin, reajusta flujo)
    void eliminarActividad(Long actividadId, Long usuarioId);

    // Consultas
    List<ActividadRegistroDTO> listarPorProceso(Long procesoId);
    ActividadRegistroDTO obtenerPorId(Long actividadId);
    boolean existePorRolProceso(Long rolProcesoId);
    List<com.edu.javeriana.backend.model.Actividad> obtenerActividadesPorRolProceso(Long rolProcesoId);
}