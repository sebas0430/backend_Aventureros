package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
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
        RolProceso rol = rolProcesoService.crearRolProceso(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", rol.getId(),
                "nombre", rol.getNombre(),
                "descripcion", rol.getDescripcion() != null ? rol.getDescripcion() : "",
                "empresaId", rol.getEmpresa().getId(),
                "mensaje", "Rol de proceso creado exitosamente"
        ));
    }

    // GET /api/roles-proceso/empresa/{empresaId}?usuarioId=X — Listar roles de proceso por empresa
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<RolProceso>> listarRolesPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.listarRolesPorEmpresa(empresaId, usuarioId));
    }

    // GET /api/roles-proceso/{id} — Obtener un rol de proceso por ID
    @GetMapping("/{id}")
    public ResponseEntity<RolProceso> obtenerRolProceso(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.obtenerRolProcesoPorId(id));
    }
}
