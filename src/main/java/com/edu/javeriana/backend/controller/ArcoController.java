package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.service.interfaces.IArcoService;
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
    public ResponseEntity<?> crearArco(@Valid @RequestBody ArcoRegistroDTO dto) {
        Arco arco = arcoService.crearArco(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", arco.getId(),
                "procesoId", dto.getProcesoId(),
                "origenId", arco.getOrigenId(),
                "origenTipo", arco.getOrigenTipo(),
                "destinoId", arco.getDestinoId(),
                "destinoTipo", arco.getDestinoTipo(),
                "etiqueta", arco.getEtiqueta() != null ? arco.getEtiqueta() : "",
                "mensaje", "Arco creado exitosamente"
        ));
    }

    // PUT /api/arcos/{id} — Editar un arco existente (cambiar origen/destino)
    @PutMapping("/{id}")
    public ResponseEntity<?> editarArco(@PathVariable Long id,
                                        @Valid @RequestBody ArcoEdicionDTO dto) {
        Arco arco = arcoService.editarArco(id, dto);
        return ResponseEntity.ok(Map.of(
                "id", arco.getId(),
                "origenId", arco.getOrigenId(),
                "origenTipo", arco.getOrigenTipo(),
                "destinoId", arco.getDestinoId(),
                "destinoTipo", arco.getDestinoTipo(),
                "etiqueta", arco.getEtiqueta() != null ? arco.getEtiqueta() : "",
                "mensaje", "Arco editado exitosamente"
        ));
    }

    // GET /api/arcos/proceso/{procesoId} — Listar todos los arcos de un proceso
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<Arco>> listarArcosPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(arcoService.listarArcosPorProceso(procesoId));
    }

    // GET /api/arcos/{id} — Obtener un arco por ID
    @GetMapping("/{id}")
    public ResponseEntity<Arco> obtenerArco(@PathVariable Long id) {
        return ResponseEntity.ok(arcoService.obtenerArcoPorId(id));
    }

    // DELETE /api/arcos/{id}?usuarioId=X — Eliminar un arco específico (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarArco(@PathVariable Long id, @RequestParam Long usuarioId) {
        arcoService.eliminarArco(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Arco eliminado exitosamente"));
    }

    // DELETE /api/arcos/proceso/{procesoId}?usuarioId=X — Eliminar todos los arcos de un proceso (solo admin)
    @DeleteMapping("/proceso/{procesoId}")
    public ResponseEntity<?> eliminarArcosPorProceso(@PathVariable Long procesoId, @RequestParam Long usuarioId) {
        arcoService.eliminarArcosPorProceso(procesoId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todos los arcos del proceso fueron eliminados"));
    }
}
