package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.model.Gateway;

import java.util.List;

public interface IGatewayService {

    Gateway crearGateway(GatewayRegistroDTO dto);

    List<Gateway> listarGatewaysPorProceso(Long procesoId);
}
