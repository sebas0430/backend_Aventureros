package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.dto.NotificacionExternaDTO;
import com.edu.javeriana.backend.service.INotificacionExternaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones-externas")
@RequiredArgsConstructor
public class NotificacionExternaController {

    private final INotificacionExternaService notificacionExternaService;

    // ===================== Conectores =====================

    @PostMapping("/conectores")
    public ResponseEntity<ConectorExternoRegistroDTO> crearConector(
            @Valid @RequestBody ConectorExternoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionExternaService.crearConector(dto));
    }

    @PutMapping("/conectores/{id}")
    public ResponseEntity<ConectorExternoRegistroDTO> editarConector(
            @PathVariable Long id,
            @Valid @RequestBody ConectorExternoRegistroDTO dto) {
        return ResponseEntity.ok(notificacionExternaService.editarConector(id, dto));
    }

    @DeleteMapping("/conectores/{id}")
    public ResponseEntity<Map<String, String>> eliminarConector(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        notificacionExternaService.eliminarConector(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Conector eliminado exitosamente"));
    }

    @GetMapping("/conectores/empresa/{empresaId}")
    public ResponseEntity<List<ConectorExternoRegistroDTO>> listarConectores(
            @PathVariable Long empresaId) {
        return ResponseEntity.ok(notificacionExternaService.listarConectoresPorEmpresa(empresaId));
    }

    // ===================== Envío =====================

    @PostMapping("/enviar")
    public ResponseEntity<NotificacionExternaDTO> enviarMensaje(
            @Valid @RequestBody EnvioExternoDTO dto) {
        return ResponseEntity.ok(notificacionExternaService.enviarMensajeExterno(dto));
    }

    // ===================== Logs =====================

    @GetMapping("/logs/proceso/{procesoId}")
    public ResponseEntity<List<NotificacionExternaDTO>> logsPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(notificacionExternaService.listarLogsPorProceso(procesoId));
    }

    @GetMapping("/logs/conector/{conectorId}")
    public ResponseEntity<List<NotificacionExternaDTO>> logsPorConector(@PathVariable Long conectorId) {
        return ResponseEntity.ok(notificacionExternaService.listarLogsPorConector(conectorId));
    }
}