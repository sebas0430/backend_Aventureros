package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.model.ConectorExterno;
import com.edu.javeriana.backend.model.NotificacionExterna;
import com.edu.javeriana.backend.service.interfaces.INotificacionExternaService;
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
    public ResponseEntity<?> crearConector(@Valid @RequestBody ConectorExternoRegistroDTO dto) {
        ConectorExterno conector = notificacionExternaService.crearConector(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", conector.getId(),
                "nombre", conector.getNombre(),
                "tipo", conector.getTipo(),
                "destino", conector.getDestino(),
                "mensaje", "Conector externo creado exitosamente"
        ));
    }

    @PutMapping("/conectores/{id}")
    public ResponseEntity<?> editarConector(@PathVariable Long id, @Valid @RequestBody ConectorExternoRegistroDTO dto) {
        ConectorExterno conector = notificacionExternaService.editarConector(id, dto);
        return ResponseEntity.ok(Map.of(
                "id", conector.getId(),
                "nombre", conector.getNombre(),
                "mensaje", "Conector editado exitosamente"
        ));
    }

    @DeleteMapping("/conectores/{id}")
    public ResponseEntity<?> eliminarConector(@PathVariable Long id, @RequestParam Long usuarioId) {
        notificacionExternaService.eliminarConector(id, usuarioId);
        return ResponseEntity.ok(Map.of("mensaje", "Conector eliminado exitosamente"));
    }

    @GetMapping("/conectores/empresa/{empresaId}")
    public ResponseEntity<List<ConectorExterno>> listarConectores(@PathVariable Long empresaId) {
        return ResponseEntity.ok(notificacionExternaService.listarConectoresPorEmpresa(empresaId));
    }

    // ===================== Envío =====================

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@Valid @RequestBody EnvioExternoDTO dto) {
        try {
            NotificacionExterna resultado = notificacionExternaService.enviarMensajeExterno(dto);
            return ResponseEntity.ok(Map.of(
                    "id", resultado.getId(),
                    "estado", resultado.getEstado(),
                    "intentos", resultado.getIntentosRealizados(),
                    "mensaje", "Envío procesado"
            ));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== Logs =====================

    @GetMapping("/logs/proceso/{procesoId}")
    public ResponseEntity<List<NotificacionExterna>> logsPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(notificacionExternaService.listarLogsPorProceso(procesoId));
    }

    @GetMapping("/logs/conector/{conectorId}")
    public ResponseEntity<List<NotificacionExterna>> logsPorConector(@PathVariable Long conectorId) {
        return ResponseEntity.ok(notificacionExternaService.listarLogsPorConector(conectorId));
    }
}
