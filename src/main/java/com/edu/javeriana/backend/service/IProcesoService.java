package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.Proceso;

import java.util.List;

public interface IProcesoService {
    Proceso crearProceso(ProcesoRegistroDTO dto);

    List<Proceso> listarPorEmpresa(Long empresaId);

    List<Proceso> listarPorAutor(Long autorId);

    Proceso actualizarDefinicion(Long procesoId, String definicionJson);

    Proceso editarProceso(Long id, com.edu.javeriana.backend.dto.ProcesoEdicionDTO dto);

    void eliminarProceso(Long procesoId, Long usuarioId);

    Proceso cambiarEstado(Long procesoId, com.edu.javeriana.backend.model.EstadoProceso nuevoEstado, Long usuarioId);
}