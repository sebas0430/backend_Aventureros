package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.service.IProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final IProcesoService procesoService;

    // POST /api/procesos
    @PostMapping
    public ResponseEntity<?> crearProceso(@Valid @RequestBody ProcesoRegistroDTO dto) {
        try {
            Proceso proceso = procesoService.crearProceso(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id",        proceso.getId(),
                    "titulo",    proceso.getTitulo(),
                    "updatedAt", proceso.getUpdatedAt(),
                    "mensaje",   "Proceso creado exitosamente"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/procesos/empresa/{empresaId}
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Proceso>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(procesoService.listarPorEmpresa(empresaId));
    }

    // GET /api/procesos/autor/{autorId}
    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<Proceso>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(procesoService.listarPorAutor(autorId));
    }

    // PATCH /api/procesos/{id}/definicion
    @PatchMapping("/{id}/definicion")
    public ResponseEntity<?> actualizarDefinicion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String definicionJson = body.get("definicionJson");
            Proceso actualizado = procesoService.actualizarDefinicion(id, definicionJson);
            return ResponseEntity.ok(Map.of(
                    "id",          actualizado.getId(),
                    "updatedAt",   actualizado.getUpdatedAt(),
                    "mensaje",     "Definición actualizada exitosamente"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/procesos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id) {
        try {
            procesoService.eliminarProceso(id);
            return ResponseEntity.ok(Map.of("mensaje", "Proceso eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/procesos/{id}/estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        com.edu.javeriana.backend.model.EstadoProceso nuevoEstado = 
            com.edu.javeriana.backend.model.EstadoProceso.valueOf((String) body.get("estado"));
        Long usuarioId = ((Number) body.get("usuarioId")).longValue();

        Proceso actualizado = procesoService.cambiarEstado(id, nuevoEstado, usuarioId);

        return ResponseEntity.ok(Map.of(
                "id",        actualizado.getId(),
                "estado",    actualizado.getEstado(),
                "mensaje",   "Estado de proceso actualizado exitosamente"
        ));
    }
}
