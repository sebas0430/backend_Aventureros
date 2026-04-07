package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IArcoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/arcos")
@RequiredArgsConstructor
public class ArcoController {

    private final IArcoService arcoService;

    // Aquí es donde creamos las "flechitas" que conectan los pasos del proceso.
    @PostMapping
    public ResponseEntity<ArcoRegistroDTO> crearArco(@Valid @RequestBody ArcoRegistroDTO dto) {
        ArcoRegistroDTO response = arcoService.crearArco(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Por si te equivocaste y quieres que la flecha apunte a otro lado.
    @PutMapping("/{id}")
    public ResponseEntity<ArcoEdicionDTO> editarArco(
            @PathVariable Long id,
            @Valid @RequestBody ArcoEdicionDTO dto) {
        ArcoEdicionDTO response = arcoService.editarArco(id, dto);
        return ResponseEntity.ok(response);
    }

    // Trae todas las conexiones de un proceso para armar el mapa en el frente.
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<ArcoRegistroDTO>> listarArcosPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(arcoService.listarArcosPorProceso(procesoId));
    }

    // Trae los datos de una sola conexión por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<ArcoRegistroDTO> obtenerArco(@PathVariable Long id) {
        return ResponseEntity.ok(arcoService.obtenerArcoPorId(id));
    }

    // Borra una flecha específica (ojo: solo si eres administrador).
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarArco(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        arcoService.eliminarArco(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Arco eliminado exitosamente"));
    }

    // Función de pánico: borra todas las conexiones de un proceso para empezar de cero.
    @DeleteMapping("/proceso/{procesoId}")
    public ResponseEntity<Map<String, String>> eliminarArcosPorProceso(
            @PathVariable Long procesoId,
            @RequestParam Long usuarioId) {
        arcoService.eliminarArcosPorProceso(procesoId, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Todos los arcos del proceso fueron eliminados"));
    }
}
