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
    public ResponseEntity<?> crearLane(@Valid @RequestBody LaneRegistroDTO dto) {
        Lane lane = laneService.crearLane(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", lane.getId(),
                "nombre", lane.getNombre(),
                "poolId", lane.getPool().getId(),
                "mensaje", "Lane (Swimlane) creado exitosamente"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarLane(@PathVariable Long id, @Valid @RequestBody LaneEdicionDTO dto) {
        Lane lane = laneService.editarLane(id, dto);
        return ResponseEntity.ok(Map.of(
                "id", lane.getId(),
                "nombre", lane.getNombre(),
                "poolId", lane.getPool().getId(),
                "mensaje", "Lane editado exitosamente"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarLane(@PathVariable Long id, @RequestParam Long usuarioId) {
        laneService.eliminarLane(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Lane eliminado exitosamente"));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<Lane>> listarLanesPorPool(
            @PathVariable Long poolId, 
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(laneService.listarLanesPorPool(poolId, usuarioId));
    }
}
