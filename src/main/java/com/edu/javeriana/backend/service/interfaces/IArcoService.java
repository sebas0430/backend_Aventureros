package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;

import java.util.List;

public interface IArcoService {

    ArcoRegistroDTO crearArco(ArcoRegistroDTO dto);

    ArcoEdicionDTO editarArco(Long id, ArcoEdicionDTO dto);

    List<ArcoRegistroDTO> listarArcosPorProceso(Long procesoId);

    ArcoRegistroDTO obtenerArcoPorId(Long id);

    void eliminarArco(Long id, Long usuarioId);

    void eliminarArcosPorProceso(Long procesoId, Long usuarioId);

    void eliminarArcosPorNodo(Long procesoId, Long nodoId, com.edu.javeriana.backend.model.TipoNodo tipoNodo);
}
