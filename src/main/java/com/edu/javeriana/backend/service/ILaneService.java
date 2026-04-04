package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;

import java.util.List;

public interface ILaneService {

     LaneRegistroDTO crearLane(LaneRegistroDTO dto);
 
    LaneEdicionDTO editarLane(Long id, LaneEdicionDTO dto);
 
    void eliminarLane(Long id, Long usuarioId);
 
    List<LaneRegistroDTO> listarLanesPorPool(Long poolId, Long usuarioId);
}
