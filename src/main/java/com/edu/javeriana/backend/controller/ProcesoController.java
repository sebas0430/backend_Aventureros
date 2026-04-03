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
    public ResponseEntity<ProcesoRegistroDTO> crearProceso(@Valid @RequestBody ProcesoRegistroDTO dto) {
        Proceso proceso = procesoService.crearProceso(dto);
        ProcesoRegistroDTO respuesta = new ProcesoRegistroDTO();
        respuesta.setNombre(proceso.getNombre());
        respuesta.setDescripcion(proceso.getDescripcion());
        respuesta.setCategoria(proceso.getCategoria());
        respuesta.setEmpresaId(proceso.getAutor().getEmpresa().getId());
        respuesta.setAutorId(proceso.getAutor().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET /api/procesos/empresa/{empresaId}
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<?> listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String categoria) {
        try {
            if (estado != null || categoria != null) {
                return ResponseEntity.ok(procesoService.filtrarProcesos(empresaId, estado, categoria));
            }
            return ResponseEntity.ok(procesoService.listarPorEmpresa(empresaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/procesos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProceso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(procesoService.obtenerProcesoPorId(id));
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
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
                    "id", actualizado.getId(),
                    "updatedAt", actualizado.getUpdatedAt(),
                    "mensaje", "Definición actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/procesos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> editarProceso(
            @PathVariable Long id,
            @Valid @RequestBody com.edu.javeriana.backend.dto.ProcesoEdicionDTO dto) {
        try {
            Proceso actualizado = procesoService.editarProceso(id, dto);
            return ResponseEntity.ok(Map.of(
                    "id", actualizado.getId(),
                    "nombre", actualizado.getNombre(),
                    "descripcion", actualizado.getDescripcion(),
                    "categoria", actualizado.getCategoria(),
                    "updatedAt", actualizado.getUpdatedAt(),
                    "mensaje", "Proceso editado exitosamente"));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // DELETE /api/procesos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProceso(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            procesoService.eliminarProceso(id, usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Proceso invalidado/eliminado exitosamente"));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // PATCH /api/procesos/{id}/estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        com.edu.javeriana.backend.model.EstadoProceso nuevoEstado = com.edu.javeriana.backend.model.EstadoProceso
                .valueOf((String) body.get("estado"));
        Long usuarioId = ((Number) body.get("usuarioId")).longValue();

        Proceso actualizado = procesoService.cambiarEstado(id, nuevoEstado, usuarioId);

        return ResponseEntity.ok(Map.of(
                "id", actualizado.getId(),
                "estado", actualizado.getEstado(),
                "mensaje", "Estado de proceso actualizado exitosamente"));
    }

    // POST /api/procesos/{id}/compartir
    @PostMapping("/{id}/compartir")
    public ResponseEntity<?> compartirProceso(
            @PathVariable Long id,
            @Valid @RequestBody com.edu.javeriana.backend.dto.ProcesoCompartirDTO dto) {
        try {
            procesoService.compartirProceso(id, dto);
            return ResponseEntity.ok(Map.of("mensaje", "Proceso compartido exitosamente con el Pool."));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // DELETE /api/procesos/{id}/compartir/{poolDestinoId}
    @DeleteMapping("/{id}/compartir/{poolDestinoId}")
    public ResponseEntity<?> quitarComparticionProceso(
            @PathVariable Long id,
            @PathVariable Long poolDestinoId,
            @RequestParam Long usuarioId) {
        try {
            procesoService.quitarComparticionProceso(id, poolDestinoId, usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Compartición revocada exitosamente."));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/procesos/compartidos/{poolId}
    @GetMapping("/compartidos/{poolId}")
    public ResponseEntity<?> listarProcesosCompartidosConPool(
            @PathVariable Long poolId,
            @RequestParam Long usuarioId) {
        try {
            return ResponseEntity.ok(procesoService.listarProcesosCompartidosConPool(poolId, usuarioId));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}