package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.RolProceso;
import com.edu.javeriana.backend.service.IRolProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles-proceso")
@RequiredArgsConstructor
public class RolProcesoController {

    private final IRolProcesoService rolProcesoService;

    // HU-17: POST /api/roles-proceso — Crear un rol de proceso
    @PostMapping
    public ResponseEntity<?> crearRolProceso(@Valid @RequestBody RolProcesoRegistroDTO dto) {
        try {
            RolProceso rol = rolProcesoService.crearRolProceso(dto);
            RolProcesoRegistroDTO respuesta = new RolProcesoRegistroDTO();
            respuesta.setNombre(rol.getNombre());
            respuesta.setDescripcion(rol.getDescripcion());
            respuesta.setEmpresaId(rol.getEmpresa().getId());
            respuesta.setUsuarioId(dto.getUsuarioId());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // HU-18: PUT /api/roles-proceso/{id} — Editar un rol de proceso
    @PutMapping("/{id}")
    public ResponseEntity<?> editarRolProceso(@PathVariable Long id, @Valid @RequestBody RolProcesoEdicionDTO dto) {
        try {
            RolProceso rol = rolProcesoService.editarRolProceso(id, dto);
            RolProcesoEdicionDTO respuesta = new RolProcesoEdicionDTO();
            respuesta.setNombre(rol.getNombre());
            respuesta.setDescripcion(rol.getDescripcion());
            respuesta.setUsuarioId(dto.getUsuarioId());
            return ResponseEntity.ok(respuesta);
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/roles-proceso/empresa/{empresaId}?usuarioId=X — Listar roles de proceso por empresa
    @GetMapping("/empresa/{empresaId}")
     public ResponseEntity<?> listarRolesPorEmpresa(@PathVariable Long empresaId, @RequestParam Long usuarioId) {
        try {
            return ResponseEntity.ok(rolProcesoService.listarRolesPorEmpresa(empresaId, usuarioId));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/roles-proceso/{id} — Obtener un rol de proceso por ID
    @GetMapping("/{id}")
    public ResponseEntity<RolProceso> obtenerRolProceso(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.obtenerRolProcesoPorId(id));
    }

    // HU-19: DELETE /api/roles-proceso/{id}?usuarioId=X — Eliminar un rol de proceso
    // La confirmación previa a la eliminación se gestiona en el frontend.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRolProceso(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            rolProcesoService.eliminarRolProceso(id, usuarioId);
            return ResponseEntity.ok("Rol de proceso eliminado exitosamente");
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // HU-20: GET /api/roles-proceso/empresa/{empresaId}/detalle?usuarioId=X
    // Listado con nombre, descripción y en qué procesos/actividades se utiliza cada rol
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<RolProcesoDetalleDTO>> consultarRolesConDetalle(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.consultarRolesConDetalle(empresaId, usuarioId));
    }

    // HU-20: GET /api/roles-proceso/{id}/detalle — Detalle de un rol específico con su uso
    @GetMapping("/{id}/detalle")
    public ResponseEntity<RolProcesoDetalleDTO> consultarRolProcesoDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.consultarRolProcesoDetalle(id));
    }
}
