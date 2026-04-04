package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.EstadoProceso;
import com.edu.javeriana.backend.model.Proceso;

import java.util.List;

public interface IProcesoService {

    ProcesoRegistroDTO crearProceso(ProcesoRegistroDTO dto);
 
    List<Proceso> listarPorEmpresa(Long empresaId);
 
    List<Proceso> listarPorAutor(Long autorId);
 
    Proceso obtenerProcesoPorId(Long id);
 
    List<Proceso> filtrarProcesos(Long empresaId, String estado, String categoria);
 
    ProcesoEdicionDTO actualizarDefinicion(Long procesoId, String definicionJson);
 
    ProcesoEdicionDTO editarProceso(Long id, ProcesoEdicionDTO dto);
 
    void eliminarProceso(Long procesoId, Long usuarioId);
 
    ProcesoEdicionDTO cambiarEstado(Long procesoId, EstadoProceso nuevoEstado, Long usuarioId);
 
    void compartirProceso(Long procesoId, ProcesoCompartirDTO dto);
 
    void quitarComparticionProceso(Long procesoId, Long poolDestinoId, Long usuarioId);
 
    List<Proceso> listarProcesosCompartidosConPool(Long poolId, Long usuarioId);
}