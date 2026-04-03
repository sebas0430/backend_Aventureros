package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.service.IGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateways")
@RequiredArgsConstructor
public class GatewayController {

    private final IGatewayService gatewayService;

    // POST /api/gateways — Crear un gateway (solo autor o admin)
    @PostMapping
    public ResponseEntity<GatewayRegistroDTO> crearGateway(
            @Valid @RequestBody GatewayRegistroDTO dto) {
        GatewayRegistroDTO response = gatewayService.crearGateway(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/gateways/{id} — Editar un gateway existente
    @PutMapping("/{id}")
    public ResponseEntity<GatewayEdicionDTO> editarGateway(
            @PathVariable Long id,
            @Valid @RequestBody GatewayEdicionDTO dto) {
        GatewayEdicionDTO response = gatewayService.editarGateway(id, dto);
        return ResponseEntity.ok(response);
    }

    // GET /api/gateways/proceso/{procesoId} — Listar todos los gateways de un proceso
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<GatewayRegistroDTO>> listarGatewaysPorProceso(
            @PathVariable Long procesoId) {
        return ResponseEntity.ok(gatewayService.listarGatewaysPorProceso(procesoId));
    }

    // DELETE /api/gateways/{id}?usuarioId=X — Eliminar un gateway (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarGateway(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        gatewayService.eliminarGateway(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Gateway eliminado exitosamente"));
    }

    // DELETE /api/gateways/proceso/{procesoId}?usuarioId=X — Eliminar todos los gateways de un proceso (solo admin)
    @DeleteMapping("/proceso/{procesoId}")
    public ResponseEntity<Map<String, String>> eliminarGatewaysPorProceso(
            @PathVariable Long procesoId,
            @RequestParam Long usuarioId) {
        gatewayService.eliminarGatewaysPorProceso(procesoId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todos los gateways del proceso fueron eliminados"));
    }
}
