package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IRolPoolService;
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
    public ResponseEntity<RolPoolRegistroDTO> crearRol(@Valid @RequestBody RolPoolRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolPoolService.crearRol(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolPoolRegistroDTO> editarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolPoolRegistroDTO dto) {
        return ResponseEntity.ok(rolPoolService.editarRol(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarRol(
            @PathVariable Long id,
            @RequestParam Long usuarioSolicitanteId) {
        rolPoolService.eliminarRol(id, usuarioSolicitanteId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol eliminado exitosamente"));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<List<RolPoolRegistroDTO>> listarRolesPorPool(
            @PathVariable Long poolId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolPoolService.listarRolesPorPool(poolId, usuarioId));
    }

    @PostMapping("/asignar")
    public ResponseEntity<AsignacionRolDTO> asignarRolAUsuario(@Valid @RequestBody AsignacionRolDTO dto) {
        return ResponseEntity.ok(rolPoolService.asignarRolAUsuario(dto));
    }

    @DeleteMapping("/desasignar")
    public ResponseEntity<Map<String, String>> desasignarRol(
            @RequestParam Long usuarioDestinoId,
            @RequestParam Long poolId,
            @RequestParam Long usuarioId) {
        rolPoolService.desasignarRolAUsuario(usuarioDestinoId, poolId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol desasignado correctamente"));
    }

    @GetMapping("/usuario/{usuarioDestinoId}/pool/{poolId}")
    public ResponseEntity<AsignacionRolDTO> obtenerRolDeUsuario(
            @PathVariable Long usuarioDestinoId,
            @PathVariable Long poolId) {
        AsignacionRolDTO asignacion = rolPoolService.obtenerAsignacionUsuario(usuarioDestinoId, poolId);
        if (asignacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(asignacion);
    }
}
