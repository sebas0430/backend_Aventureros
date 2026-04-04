package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.model.Arco;

import java.util.List;

public interface IArcoService {

    Arco crearArco(ArcoRegistroDTO dto);

    Arco editarArco(Long id, ArcoEdicionDTO dto);

    List<Arco> listarArcosPorProceso(Long procesoId);

    Arco obtenerArcoPorId(Long id);

    void eliminarArco(Long id, Long usuarioId);

    void eliminarArcosPorNodo(Long procesoId, Long nodoId, com.edu.javeriana.backend.model.TipoNodo tipoNodo);

    void eliminarArcosPorProceso(Long procesoId, Long usuarioId);
}
