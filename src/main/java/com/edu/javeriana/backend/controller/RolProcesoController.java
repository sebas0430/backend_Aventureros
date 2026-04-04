package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
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

    // HU-17: POST /api/roles-proceso
    @PostMapping
    public ResponseEntity<RolProcesoRegistroDTO> crearRolProceso(
            @Valid @RequestBody RolProcesoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rolProcesoService.crearRolProceso(dto));
    }

    // HU-18: PUT /api/roles-proceso/{id}
    @PutMapping("/{id}")
    public ResponseEntity<RolProcesoEdicionDTO> editarRolProceso(
            @PathVariable Long id,
            @Valid @RequestBody RolProcesoEdicionDTO dto) {
        return ResponseEntity.ok(rolProcesoService.editarRolProceso(id, dto));
    }

    // GET /api/roles-proceso/empresa/{empresaId}?usuarioId=X
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<RolProcesoRegistroDTO>> listarRolesPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.listarRolesPorEmpresa(empresaId, usuarioId));
    }

    // GET /api/roles-proceso/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RolProcesoRegistroDTO> obtenerRolProceso(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.obtenerRolProcesoPorId(id));
    }

    // HU-19: DELETE /api/roles-proceso/{id}?usuarioId=X
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarRolProceso(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        rolProcesoService.eliminarRolProceso(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol de proceso eliminado exitosamente"));
    }

    // HU-20: GET /api/roles-proceso/empresa/{empresaId}/detalle?usuarioId=X
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<RolProcesoDetalleDTO>> consultarRolesConDetalle(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.consultarRolesConDetalle(empresaId, usuarioId));
    }

    // HU-20: GET /api/roles-proceso/{id}/detalle
    @GetMapping("/{id}/detalle")
    public ResponseEntity<RolProcesoDetalleDTO> consultarRolProcesoDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.consultarRolProcesoDetalle(id));
    }
}