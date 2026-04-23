package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.EstadoProceso;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
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

    // Crea un proceso vacío para empezar a trabajar en él.
    @PostMapping
    public ResponseEntity<?> crearProceso(@Valid @RequestBody ProcesoRegistroDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(procesoService.crearProceso(dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    // Lista los procesos de una empresa, con opción de filtrar por estado o categoría.
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ProcesoRegistroDTO>> listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String categoria) {
        try {
            if (estado != null || categoria != null) {
                return ResponseEntity.ok(procesoService.filtrarProcesos(empresaId, estado, categoria));
            }
            return ResponseEntity.ok(procesoService.listarPorEmpresa(empresaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Dame toda la info de un solo proceso por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<ProcesoRegistroDTO> obtenerProceso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(procesoService.obtenerProcesoPorId(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Lista los procesos que ha creado un usuario específico (el autor).
    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<ProcesoRegistroDTO>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(procesoService.listarPorAutor(autorId));
    }

    // ¡IMPORTANTE! Este guarda el JSON que define todo el dibujo del proceso.
    @PatchMapping("/{id}/definicion")
    public ResponseEntity<?> actualizarDefinicion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String definicionJson = body.get("definicionJson");
            if (definicionJson == null) {
                return ResponseEntity.badRequest().body("El campo definicionJson es requerido");
            }
            ProcesoEdicionDTO resultado = procesoService.actualizarDefinicion(id, definicionJson);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al guardar: " + e.getMessage());
        }
    }

    // Cambia los datos generales (nombre, descripción) de un proceso.
    @PutMapping("/{id}")
    public ResponseEntity<ProcesoEdicionDTO> editarProceso(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoEdicionDTO dto) {
        try {
            return ResponseEntity.ok(procesoService.editarProceso(id, dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Elimina un proceso (lo pone en modo INACTIVO).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProceso(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            procesoService.eliminarProceso(id, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Cambia el estado (BORRADOR, PUBLICADO, etc.) del proceso.
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProcesoEdicionDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            EstadoProceso nuevoEstado = EstadoProceso.valueOf((String) body.get("estado"));
            Long usuarioId = ((Number) body.get("usuarioId")).longValue();
            return ResponseEntity.ok(procesoService.cambiarEstado(id, nuevoEstado, usuarioId));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Comparte el proceso con otro Pool para que puedan verlo u operarlo.
    @PostMapping("/{id}/compartir")
    public ResponseEntity<ProcesoCompartirDTO> compartirProceso(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoCompartirDTO dto) {
        try {
            procesoService.compartirProceso(id, dto);
            return ResponseEntity.ok(dto);
        } catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Quita el permiso de un Pool sobre un proceso compartido.
    @DeleteMapping("/{id}/compartir/{poolDestinoId}")
    public ResponseEntity<Void> quitarComparticionProceso(
            @PathVariable Long id,
            @PathVariable Long poolDestinoId,
            @RequestParam Long usuarioId) {
        try {
            procesoService.quitarComparticionProceso(id, poolDestinoId, usuarioId);
            return ResponseEntity.noContent().build();
        } catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Lista todos los procesos que le han compartido a un Pool.
    @GetMapping("/compartidos/{poolId}")
    public ResponseEntity<List<ProcesoRegistroDTO>> listarProcesosCompartidosConPool(
            @PathVariable Long poolId,
            @RequestParam Long usuarioId) {
        try {
            return ResponseEntity.ok(procesoService.listarProcesosCompartidosConPool(poolId, usuarioId));
        } catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}