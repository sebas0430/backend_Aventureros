package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.RolPool;
import com.edu.javeriana.backend.service.IRolPoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles-pool")
@RequiredArgsConstructor
public class RolPoolController {

    private final IRolPoolService rolPoolService;

    @PostMapping
    public ResponseEntity<?> crearRol(@Valid @RequestBody RolPoolRegistroDTO dto) {
        try {
            RolPool rol = rolPoolService.crearRol(dto);
            RolPoolRegistroDTO respuesta = new RolPoolRegistroDTO();
            respuesta.setNombre(rol.getNombre());
            respuesta.setDescripcion(rol.getDescripcion());
            respuesta.setPoolId(rol.getPool().getId());
            respuesta.setPermisoCrearProceso(rol.isPermisoCrearProceso());
            respuesta.setPermisoEditarProceso(rol.isPermisoEditarProceso());
            respuesta.setPermisoEliminarProceso(rol.isPermisoEliminarProceso());
            respuesta.setPermisoPublicarProceso(rol.isPermisoPublicarProceso());
            respuesta.setPermisoGestionarRoles(rol.isPermisoGestionarRoles());
            respuesta.setUsuarioId(dto.getUsuarioId());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarRol(@PathVariable Long id, @Valid @RequestBody RolPoolRegistroDTO dto) {
        try {
            RolPool rol = rolPoolService.editarRol(id, dto);
            RolPoolRegistroDTO respuesta = new RolPoolRegistroDTO();
            respuesta.setNombre(rol.getNombre());
            respuesta.setDescripcion(rol.getDescripcion());
            respuesta.setPoolId(rol.getPool().getId());
            respuesta.setPermisoCrearProceso(rol.isPermisoCrearProceso());
            respuesta.setPermisoEditarProceso(rol.isPermisoEditarProceso());
            respuesta.setPermisoEliminarProceso(rol.isPermisoEliminarProceso());
            respuesta.setPermisoPublicarProceso(rol.isPermisoPublicarProceso());
            respuesta.setPermisoGestionarRoles(rol.isPermisoGestionarRoles());
            respuesta.setUsuarioId(dto.getUsuarioId());
            return ResponseEntity.ok(respuesta);
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id, @RequestParam Long usuarioSolicitanteId) {
        try {
            rolPoolService.eliminarRol(id, usuarioSolicitanteId);
            return ResponseEntity.ok("Rol eliminado exitosamente");
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<RolPool>> listarRolesPorPool(@PathVariable Long poolId, @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolPoolService.listarRolesPorPool(poolId, usuarioId));
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarRolAUsuario(@Valid @RequestBody AsignacionRolDTO dto) {
        try {
            AsignacionRolPool asignacion = rolPoolService.asignarRolAUsuario(dto);
            AsignacionRolDTO respuesta = new AsignacionRolDTO();
            respuesta.setUsuarioDestinoId(asignacion.getUsuario().getId());
            respuesta.setRolPoolId(asignacion.getRol().getId());
            respuesta.setPoolId(asignacion.getPool().getId());
            respuesta.setUsuarioId(dto.getUsuarioId());
            return ResponseEntity.ok(respuesta);
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/desasignar")
    public ResponseEntity<?> desasignarRol(
            @RequestParam Long usuarioDestinoId,
            @RequestParam Long poolId,
            @RequestParam Long usuarioId) {
        try {
            rolPoolService.desasignarRolAUsuario(usuarioDestinoId, poolId, usuarioId);
            return ResponseEntity.ok("Rol desasignado correctamente");
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioDestinoId}/pool/{poolId}")
    public ResponseEntity<?> obtenerRolDeUsuario(@PathVariable Long usuarioDestinoId, @PathVariable Long poolId) {
        try {
            AsignacionRolPool asignacion = rolPoolService.obtenerAsignacionUsuario(usuarioDestinoId, poolId);
            if (asignacion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no tiene ningún rol asignado en este pool");
            }
            return ResponseEntity.ok(asignacion.getRol());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
