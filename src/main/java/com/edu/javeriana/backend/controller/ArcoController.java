package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.service.IArcoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/arcos")
@RequiredArgsConstructor
public class ArcoController {

    private final IArcoService arcoService;

    // POST /api/arcos — Crear un arco entre dos nodos
    @PostMapping
    public ResponseEntity<ArcoRegistroDTO> crearArco(@Valid @RequestBody ArcoRegistroDTO dto) {
        ArcoRegistroDTO response = arcoService.crearArco(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/arcos/{id} — Editar un arco existente (cambiar origen/destino)
    @PutMapping("/{id}")
    public ResponseEntity<ArcoEdicionDTO> editarArco(
            @PathVariable Long id,
            @Valid @RequestBody ArcoEdicionDTO dto) {
        ArcoEdicionDTO response = arcoService.editarArco(id, dto);
        return ResponseEntity.ok(response);
    }

    // GET /api/arcos/proceso/{procesoId} — Listar todos los arcos de un proceso
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<ArcoRegistroDTO>> listarArcosPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(arcoService.listarArcosPorProceso(procesoId));
    }

    // GET /api/arcos/{id} — Obtener un arco por ID
    @GetMapping("/{id}")
    public ResponseEntity<ArcoRegistroDTO> obtenerArco(@PathVariable Long id) {
        return ResponseEntity.ok(arcoService.obtenerArcoPorId(id));
    }

    // DELETE /api/arcos/{id}?usuarioId=X — Eliminar un arco específico (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarArco(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        arcoService.eliminarArco(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Arco eliminado exitosamente"));
    }

    // DELETE /api/arcos/proceso/{procesoId}?usuarioId=X — Eliminar todos los arcos de un proceso (solo admin)
    @DeleteMapping("/proceso/{procesoId}")
    public ResponseEntity<Map<String, String>> eliminarArcosPorProceso(
            @PathVariable Long procesoId,
            @RequestParam Long usuarioId) {
        arcoService.eliminarArcosPorProceso(procesoId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todos los arcos del proceso fueron eliminados"));
    }
}
