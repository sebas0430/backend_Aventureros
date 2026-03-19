package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.model.RecepcionMensaje;
import com.edu.javeriana.backend.service.IMessageCatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message-catch")
@RequiredArgsConstructor
public class MessageCatchController {

    private final IMessageCatchService messageCatchService;

    /**
     * Endpoint para recibir un mensaje (interno o externo) y activar/continuar
     * procesos que tengan un CATCH esperando con ese nombre de mensaje.
     */
    @PostMapping("/recibir")
    public ResponseEntity<?> recibirMensaje(@Valid @RequestBody MensajeCatchDTO dto) {
        try {
            List<RecepcionMensaje> recepciones = messageCatchService.recibirMensaje(dto);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Mensaje recibido y procesado exitosamente",
                    "catchesActivados", recepciones.size(),
                    "recepciones", recepciones
            ));
        } catch (com.edu.javeriana.backend.exception.BusinessRuleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (com.edu.javeriana.backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /** Logs de recepciones por proceso */
    @GetMapping("/logs/proceso/{procesoId}")
    public ResponseEntity<List<RecepcionMensaje>> logsPorProceso(@PathVariable Long procesoId) {
        return ResponseEntity.ok(messageCatchService.listarRecepcionesPorProceso(procesoId));
    }

    /** Logs de recepciones por evento CATCH específico */
    @GetMapping("/logs/catch/{eventoCatchId}")
    public ResponseEntity<List<RecepcionMensaje>> logsPorCatch(@PathVariable Long eventoCatchId) {
        return ResponseEntity.ok(messageCatchService.listarRecepcionesPorCatch(eventoCatchId));
    }
}
