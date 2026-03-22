package com.edu.javeriana.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Actividad;
import com.edu.javeriana.backend.service.IActividadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final IActividadService actividadService;

    // ─────────────────────────────────────────────
    // HU-08: POST /api/actividades
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> crearActividad(@Valid @RequestBody ActividadRegistroDTO dto) {
        try {
            Actividad actividad = actividadService.crearActividad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", actividad.getId(),
                    "nombre", actividad.getNombre(),
                    "tipoActividad", actividad.getTipoActividad(),
                    "rolResponsable", actividad.getRolResponsable(),
                    "orden", actividad.getOrden() != null ? actividad.getOrden() : Integer.valueOf(0),
                    "procesoId", actividad.getProceso().getId(),
                    "mensaje", "Actividad creada exitosamente"));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // HU-09: PUT /api/actividades/{id}
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> editarActividad(
            @PathVariable Long id,
            @Valid @RequestBody ActividadEdicionDTO dto) {
        try {
            Actividad actividad = actividadService.editarActividad(id, dto);
            return ResponseEntity.ok(Map.of(
                    "id", actividad.getId(),
                    "nombre", actividad.getNombre(),
                    "tipoActividad", actividad.getTipoActividad(),
                    "descripcion", actividad.getDescripcion(),
                    "rolResponsable", actividad.getRolResponsable(),
                    "updatedAt", actividad.getUpdatedAt(),
                    "mensaje", "Actividad editada exitosamente"));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // HU-10: DELETE /api/actividades/{id}?usuarioId=X
    // La confirmación antes de eliminar se gestiona en el frontend;
    // el backend realiza el soft-delete y reajusta el flujo.
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarActividad(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        try {
            actividadService.eliminarActividad(id, usuarioId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Actividad eliminada y flujo del proceso reajustado exitosamente"));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/actividades/proceso/{procesoId}
    // ─────────────────────────────────────────────
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<?> listarPorProceso(@PathVariable Long procesoId) {
        try {
            return ResponseEntity.ok(actividadService.listarPorProceso(procesoId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/actividades/{id}
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerActividad(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(actividadService.obtenerPorId(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}