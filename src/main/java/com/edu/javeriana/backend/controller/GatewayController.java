package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.model.Gateway;
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
    public ResponseEntity<?> crearGateway(@Valid @RequestBody GatewayRegistroDTO dto) {
        Gateway gateway = gatewayService.crearGateway(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", gateway.getId(),
                "nombre", gateway.getNombre(),
                "tipo", gateway.getTipo().name(),
                "procesoId", gateway.getProceso().getId(),
                "mensaje", "Gateway creado exitosamente"
        ));
    }

    // GET /api/gateways/proceso/{procesoId} — Listar todos los gateways de un proceso
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<Gateway>> listarGatewaysPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(gatewayService.listarGatewaysPorProceso(procesoId));
    }
}
