package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/invitar")
    public ResponseEntity<?> invitarUsuario(@Valid @RequestBody UsuarioRegistroDTO dto) {
        try {
            Usuario nuevoUsuario = usuarioService.invitarUsuario(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("id", nuevoUsuario.getId());
            response.put("username", nuevoUsuario.getUsername());
            response.put("rol", nuevoUsuario.getRol());
            response.put("mensaje", "Usuario registrado exitosamente en la empresa");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
