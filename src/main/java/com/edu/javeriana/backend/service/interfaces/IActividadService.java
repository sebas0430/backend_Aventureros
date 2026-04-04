package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.model.Actividad;

import java.util.List;

public interface IActividadService {

    // HU-08: Crear actividad vinculada a un proceso
    Actividad crearActividad(ActividadRegistroDTO dto);

    // HU-09: Editar actividad (guarda cambios en historial del proceso)
    Actividad editarActividad(Long actividadId, ActividadEdicionDTO dto);

    // HU-10: Eliminar actividad (solo admin, reajusta flujo)
    void eliminarActividad(Long actividadId, Long usuarioId);

    // Consultas
    List<Actividad> listarPorProceso(Long procesoId);

    Actividad obtenerPorId(Long actividadId);

    boolean existeActividadPorRolProceso(Long rolProcesoId);

    List<Actividad> listarActividadesPorRolProceso(Long rolProcesoId);
}
