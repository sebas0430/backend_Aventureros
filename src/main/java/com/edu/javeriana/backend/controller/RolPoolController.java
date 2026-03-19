package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
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
        RolPool rol = rolPoolService.crearRol(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", rol.getId(),
                "nombre", rol.getNombre(),
                "poolId", rol.getPool().getId(),
                "mensaje", "Rol de pool creado exitosamente"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarRol(@PathVariable Long id, @Valid @RequestBody RolPoolRegistroDTO dto) {
        RolPool rol = rolPoolService.editarRol(id, dto);
        return ResponseEntity.ok(Map.of(
                "id", rol.getId(),
                "nombre", rol.getNombre(),
                "mensaje", "Rol actualizado exitosamente"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id, @RequestParam Long usuarioSolicitanteId) {
        rolPoolService.eliminarRol(id, usuarioSolicitanteId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol eliminado exitosamente"));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<RolPool>> listarRolesPorPool(@PathVariable Long poolId, @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolPoolService.listarRolesPorPool(poolId, usuarioId));
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarRolAUsuario(@Valid @RequestBody AsignacionRolDTO dto) {
        AsignacionRolPool asignacion = rolPoolService.asignarRolAUsuario(dto);
        return ResponseEntity.ok(Map.of(
                "id", asignacion.getId(),
                "usuarioId", asignacion.getUsuario().getId(),
                "rolNombre", asignacion.getRol().getNombre(),
                "poolId", asignacion.getPool().getId(),
                "mensaje", "Rol asignado correctamente"
        ));
    }

    @DeleteMapping("/desasignar")
    public ResponseEntity<?> desasignarRol(
            @RequestParam Long usuarioDestinoId,
            @RequestParam Long poolId,
            @RequestParam Long usuarioId) {
        rolPoolService.desasignarRolAUsuario(usuarioDestinoId, poolId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol desasignado correctamente"));
    }

    @GetMapping("/usuario/{usuarioDestinoId}/pool/{poolId}")
    public ResponseEntity<?> obtenerRolDeUsuario(@PathVariable Long usuarioDestinoId, @PathVariable Long poolId) {
        AsignacionRolPool asignacion = rolPoolService.obtenerAsignacionUsuario(usuarioDestinoId, poolId);
        if (asignacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "El usuario no tiene ningún rol asignado en este pool"));
        }
        return ResponseEntity.ok(asignacion.getRol());
    }
}
