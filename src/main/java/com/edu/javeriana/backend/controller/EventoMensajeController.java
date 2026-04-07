package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeEjecucionDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.service.interfaces.IEventoMensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos-mensaje")
@RequiredArgsConstructor
public class EventoMensajeController {

    private final IEventoMensajeService eventoMensajeService;

    // Crea un evento de tipo mensaje (un punto donde el proceso manda o espera una señal).
    @PostMapping
    public ResponseEntity<EventoMensajeRegistroDTO> crearEvento(
            @Valid @RequestBody EventoMensajeRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoMensajeService.crearEvento(dto));
    }

    // Lista todos los eventos de mensaje que tiene un proceso.
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<EventoMensajeRegistroDTO>> listarPorProceso(
            @PathVariable Long procesoId) {
        return ResponseEntity.ok(eventoMensajeService.listarPorProceso(procesoId));
    }

    // Borra un evento de mensaje.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarEvento(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        eventoMensajeService.eliminarEvento(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Evento de mensaje eliminado correctamente"));
    }

    // ¡DISPARA EL MENSAJE! Este es el que hace que la señal salga volando a otros procesos.
    @PostMapping("/lanzar")
    public ResponseEntity<List<MensajeEjecucionDTO>> lanzarMensajeThrow(
            @Valid @RequestBody MensajeLanzarDTO dto) {
        return ResponseEntity.ok(eventoMensajeService.lanzarMensaje(dto));
    }

    // Mira quién ha recibido qué mensaje (el historial de chismes entre procesos).
    @GetMapping("/logs/{eventoId}")
    public ResponseEntity<List<MensajeEjecucionDTO>> listarLogsDeLanzamiento(
            @PathVariable Long eventoId) {
        return ResponseEntity.ok(eventoMensajeService.listarHistorialPorEventoOrigen(eventoId));
    }
}