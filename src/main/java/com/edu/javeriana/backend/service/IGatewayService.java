package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;

import java.util.List;

public interface IGatewayService {

    GatewayRegistroDTO crearGateway(GatewayRegistroDTO dto);

    GatewayEdicionDTO editarGateway(Long id, GatewayEdicionDTO dto);

    List<GatewayRegistroDTO> listarGatewaysPorProceso(Long procesoId);

    void eliminarGateway(Long id, Long usuarioId);

    void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId);
}
