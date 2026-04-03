package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.model.Lane;
import com.edu.javeriana.backend.service.ILaneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lanes")
@RequiredArgsConstructor
public class LaneController {

    private final ILaneService laneService;

    @PostMapping
    public ResponseEntity<LaneRegistroDTO> crearLane(@Valid @RequestBody LaneRegistroDTO dto) {
        Lane lane = laneService.crearLane(dto);
        LaneRegistroDTO respuesta = new LaneRegistroDTO();
        respuesta.setNombre(lane.getNombre());
        respuesta.setDescripcion(lane.getDescripcion());
        respuesta.setPoolId(lane.getPool().getId());
        respuesta.setUsuarioId(dto.getUsuarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaneEdicionDTO> editarLane(
            @PathVariable Long id,
            @Valid @RequestBody LaneEdicionDTO dto) {
        Lane lane = laneService.editarLane(id, dto);
        LaneEdicionDTO respuesta = new LaneEdicionDTO();
        respuesta.setNombre(lane.getNombre());
        respuesta.setDescripcion(lane.getDescripcion());
        respuesta.setUsuarioId(dto.getUsuarioId());
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLane(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        laneService.eliminarLane(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<LaneRegistroDTO>> listarLanesPorPool(
            @PathVariable Long poolId,
            @RequestParam Long usuarioId) {
        List<LaneRegistroDTO> respuesta = laneService.listarLanesPorPool(poolId, usuarioId)
                .stream() // Convertimos cada Lane a LaneRegistroDTO para la respuesta 
                .map(lane -> {
                    LaneRegistroDTO dto = new LaneRegistroDTO(); // No se incluye el ID del usuario en la respuesta, ya que no es relevante para el cliente
                    dto.setNombre(lane.getNombre());
                    dto.setDescripcion(lane.getDescripcion());
                    dto.setPoolId(lane.getPool().getId());
                    return dto; //
                })
                .toList(); // Convertimos el Stream de LaneRegistroDTO a List<LaneRegistroDTO>
        return ResponseEntity.ok(respuesta); // Devolvemos la lista de LaneRegistroDTO como respuesta
    }
}
