package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.service.interfaces.ILaneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
@RequiredArgsConstructor
public class LaneController {

    private final ILaneService laneService;

    @PostMapping
    public ResponseEntity<LaneRegistroDTO> crearLane(@Valid @RequestBody LaneRegistroDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(laneService.crearLane(dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaneEdicionDTO> editarLane(@PathVariable Long id, @Valid @RequestBody LaneEdicionDTO dto) {
        try {
            return ResponseEntity.ok(laneService.editarLane(id, dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLane(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            laneService.eliminarLane(id, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<LaneRegistroDTO>> listarLanesPorPool(@PathVariable Long poolId, @RequestParam Long usuarioId) {
        try {
            return ResponseEntity.ok(laneService.listarLanesPorPool(poolId, usuarioId));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}