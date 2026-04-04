package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.service.IPoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
public class PoolController {

    private final IPoolService poolService;

    @PostMapping
    public ResponseEntity<PoolRegistroDTO> crearPool(@Valid @RequestBody PoolRegistroDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(poolService.crearPool(dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoolEdicionDTO> editarPool(@PathVariable Long id, @Valid @RequestBody PoolEdicionDTO dto) {
        try {
            return ResponseEntity.ok(poolService.editarPool(id, dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPool(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            poolService.eliminarPool(id, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PoolRegistroDTO>> listarPoolsPorEmpresa(@PathVariable Long empresaId) {
        try {
            return ResponseEntity.ok(poolService.listarPoolsPorEmpresa(empresaId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}