package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IRolProcesoService;
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

    // Crea un rol general para ser usado en los procesos de la empresa.
    @PostMapping
    public ResponseEntity<RolProcesoRegistroDTO> crearRolProceso(
            @Valid @RequestBody RolProcesoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rolProcesoService.crearRolProceso(dto));
    }

   
    // Edita un rol de proceso.
    @PutMapping("/{id}")
    public ResponseEntity<RolProcesoEdicionDTO> editarRolProceso(
            @PathVariable Long id,
            @Valid @RequestBody RolProcesoEdicionDTO dto) {
        return ResponseEntity.ok(rolProcesoService.editarRolProceso(id, dto));
    }

    
    // Lista todos los roles de proceso que pertenecen a una empresa.
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<RolProcesoRegistroDTO>> listarRolesPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.listarRolesPorEmpresa(empresaId, usuarioId));
    }

    
    // Obtiene un rol de proceso por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<RolProcesoRegistroDTO> obtenerRolProceso(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.obtenerRolProcesoPorId(id));
    }


    // Borra un rol de proceso.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarRolProceso(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        rolProcesoService.eliminarRolProceso(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Rol de proceso eliminado exitosamente"));
    }

    
    // Consulta roles con detalle (incluyendo en qué procesos y tareas se usan).
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<RolProcesoDetalleDTO>> consultarRolesConDetalle(
            @PathVariable Long empresaId,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(rolProcesoService.consultarRolesConDetalle(empresaId, usuarioId));
    }

    
    // Detalle profundo de un solo rol.
    @GetMapping("/{id}/detalle")
    public ResponseEntity<RolProcesoDetalleDTO> consultarRolProcesoDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.consultarRolProcesoDetalle(id));
    }
}