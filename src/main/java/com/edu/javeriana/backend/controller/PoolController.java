package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.service.IPoolService;
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
    public ResponseEntity<PoolRegistroDTO> crearPool(@Valid @RequestBody PoolRegistroDTO dto) {
        Pool pool = poolService.crearPool(dto);
        PoolRegistroDTO respuesta = new PoolRegistroDTO();
        respuesta.setNombre(pool.getNombre());
        respuesta.setDescripcion(pool.getDescripcion());
        respuesta.setEmpresaId(pool.getEmpresa().getId());
        respuesta.setUsuarioId(dto.getUsuarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoolEdicionDTO> editarPool(
            @PathVariable Long id,
            @Valid @RequestBody PoolEdicionDTO dto) {
        Pool pool = poolService.editarPool(id, dto);
        PoolEdicionDTO respuesta = new PoolEdicionDTO();
        respuesta.setNombre(pool.getNombre());
        respuesta.setDescripcion(pool.getDescripcion());
        respuesta.setUsuarioId(dto.getUsuarioId());
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPool(@PathVariable Long id, @RequestParam Long usuarioId) {
        poolService.eliminarPool(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Pool>> listarPoolsPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(poolService.listarPoolsPorEmpresa(empresaId));
    }
}
