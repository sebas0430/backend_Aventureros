package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.service.interfaces.IMessageCatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message-catch")
@RequiredArgsConstructor
public class MessageCatchController {

    private final IMessageCatchService messageCatchService;

    // Este endpoint "atrapa" mensajes que vienen de afuera para despertar procesos.
    @PostMapping("/recibir")
    public ResponseEntity<List<MensajeCatchDTO>> recibirMensaje(@Valid @RequestBody MensajeCatchDTO dto) {
        try {
            return ResponseEntity.ok(messageCatchService.recibirMensaje(dto));
        } catch (BusinessRuleException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Ver el historial de todos los mensajes atrapados en un proceso.
    @GetMapping("/logs/proceso/{procesoId}")
    public ResponseEntity<List<MensajeCatchDTO>> logsPorProceso(@PathVariable Long procesoId) {
        try {
            return ResponseEntity.ok(messageCatchService.listarRecepcionesPorProceso(procesoId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Ver el historial de mensajes atrapados por un evento catch específico.
    @GetMapping("/logs/catch/{eventoCatchId}")
    public ResponseEntity<List<MensajeCatchDTO>> logsPorCatch(@PathVariable Long eventoCatchId) {
        try {
            return ResponseEntity.ok(messageCatchService.listarRecepcionesPorCatch(eventoCatchId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}