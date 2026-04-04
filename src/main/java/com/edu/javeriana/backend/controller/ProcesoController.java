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

    @PostMapping
    public ResponseEntity<ProcesoRegistroDTO> crearProceso(@Valid @RequestBody ProcesoRegistroDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(procesoService.crearProceso(dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoRegistroDTO> obtenerProceso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(procesoService.obtenerProcesoPorId(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<ProcesoRegistroDTO>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(procesoService.listarPorAutor(autorId));
    }

    @PatchMapping("/{id}/definicion")
    public ResponseEntity<ProcesoEdicionDTO> actualizarDefinicion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(procesoService.actualizarDefinicion(id, body.get("definicionJson")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

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