package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.model.Lane;

import java.util.List;

public interface ILaneService {

    Lane crearLane(LaneRegistroDTO dto);

    Lane editarLane(Long id, LaneEdicionDTO dto);

    void eliminarLane(Long id, Long usuarioId);

    List<Lane> listarLanesPorPool(Long poolId, Long usuarioId);
}
