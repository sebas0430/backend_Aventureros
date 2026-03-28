package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.Proceso;

import java.util.List;

public interface IProcesoService {
    Proceso crearProceso(ProcesoRegistroDTO dto);

    List<Proceso> listarPorEmpresa(Long empresaId);

    List<Proceso> listarPorAutor(Long autorId);

    Proceso obtenerProcesoPorId(Long id);

    boolean existeProcesoPorId(Long id);

    List<Proceso> filtrarProcesos(Long empresaId, String estado, String categoria);

    Proceso actualizarDefinicion(Long procesoId, String definicionJson);

    Proceso editarProceso(Long id, com.edu.javeriana.backend.dto.ProcesoEdicionDTO dto);

    void eliminarProceso(Long procesoId, Long usuarioId);

    Proceso cambiarEstado(Long procesoId, com.edu.javeriana.backend.model.EstadoProceso nuevoEstado, Long usuarioId);

    void compartirProceso(Long procesoId, com.edu.javeriana.backend.dto.ProcesoCompartirDTO dto);

    void quitarComparticionProceso(Long procesoId, Long poolDestinoId, Long usuarioId);

    List<Proceso> listarProcesosCompartidosConPool(Long poolId, Long usuarioId);
}
