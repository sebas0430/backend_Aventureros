package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.EstadoProceso;

import java.util.List;

public interface IProcesoService {

    ProcesoRegistroDTO crearProceso(ProcesoRegistroDTO dto);

    List<ProcesoRegistroDTO> listarPorEmpresa(Long empresaId);

    List<ProcesoRegistroDTO> listarPorAutor(Long autorId);

    ProcesoRegistroDTO obtenerProcesoPorId(Long id);

    List<ProcesoRegistroDTO> filtrarProcesos(Long empresaId, String estado, String categoria);

    ProcesoEdicionDTO actualizarDefinicion(Long procesoId, String definicionJson);

    ProcesoEdicionDTO editarProceso(Long id, ProcesoEdicionDTO dto);

    void eliminarProceso(Long procesoId, Long usuarioId);

    ProcesoEdicionDTO cambiarEstado(Long procesoId, EstadoProceso nuevoEstado, Long usuarioId);

    void compartirProceso(Long procesoId, ProcesoCompartirDTO dto);

    void quitarComparticionProceso(Long procesoId, Long poolDestinoId, Long usuarioId);

    List<ProcesoRegistroDTO> listarProcesosCompartidosConPool(Long poolId, Long usuarioId);

    com.edu.javeriana.backend.model.Proceso obtenerProcesoEntity(Long id);

    boolean existeProceso(Long id);
}