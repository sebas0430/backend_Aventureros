package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.service.interfaces.IPoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
public class PoolController {

    private final IPoolService poolService;

    @PostMapping
    public ResponseEntity<?> crearPool(@Valid @RequestBody PoolRegistroDTO dto) {
        Pool pool = poolService.crearPool(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", pool.getId(),
                "nombre", pool.getNombre(),
                "empresaId", pool.getEmpresa().getId(),
                "mensaje", "Pool creado exitosamente"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarPool(@PathVariable Long id, @Valid @RequestBody PoolEdicionDTO dto) {
        Pool pool = poolService.editarPool(id, dto);
        return ResponseEntity.ok(Map.of(
                "id", pool.getId(),
                "nombre", pool.getNombre(),
                "empresaId", pool.getEmpresa().getId(),
                "mensaje", "Pool editado exitosamente"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPool(@PathVariable Long id, @RequestParam Long usuarioId) {
        poolService.eliminarPool(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Pool eliminado exitosamente"));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Pool>> listarPoolsPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(poolService.listarPoolsPorEmpresa(empresaId));
    }
}
