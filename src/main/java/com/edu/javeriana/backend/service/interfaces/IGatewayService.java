package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.model.Gateway;

import java.util.List;

public interface IGatewayService {

    Gateway crearGateway(GatewayRegistroDTO dto);

    Gateway editarGateway(Long id, GatewayEdicionDTO dto);

    List<Gateway> listarGatewaysPorProceso(Long procesoId);

    void eliminarGateway(Long id, Long usuarioId);

    void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId);
}
