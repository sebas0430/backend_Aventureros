package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.model.EventoMensaje;
import com.edu.javeriana.backend.model.MensajeEjecucion;
import com.edu.javeriana.backend.service.IEventoMensajeService;
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

    @PostMapping
    public ResponseEntity<?> crearEvento(@Valid @RequestBody EventoMensajeRegistroDTO dto) {
        try {
            EventoMensaje evento = eventoMensajeService.crearEvento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", evento.getId(),
                    "nombreMensaje", evento.getNombreMensaje(),
                    "tipo", evento.getTipo(),
                    "procesoId", evento.getProceso().getId(),
                    "mensaje", "Evento configurado exitosamente"
            ));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<EventoMensaje>> listarPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(eventoMensajeService.listarPorProceso(procesoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEvento(@PathVariable Long id, @RequestParam Long usuarioId) {
        try {
            eventoMensajeService.eliminarEvento(id, usuarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Evento de mensaje eliminado correctamente"));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/lanzar")
    public ResponseEntity<?> lanzarMensajeThrow(@Valid @RequestBody MensajeLanzarDTO dto) {
        try {
            List<MensajeEjecucion> logs = eventoMensajeService.lanzarMensaje(dto);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Lanzamiento procesado (Revisar logs)",
                    "impactos", logs
            ));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(Map.of("error_lanzamiento", e.getMessage()));
        }
    }
    
    @GetMapping("/logs/{eventoId}")
    public ResponseEntity<List<MensajeEjecucion>> listarLogsDeLanzamiento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(eventoMensajeService.listarHistorialPorEventoOrigen(eventoId));
    }
}
