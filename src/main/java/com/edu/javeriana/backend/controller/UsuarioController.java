package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.service.IUsuarioService;
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

    private final IUsuarioService usuarioService;

    @PostMapping("/invitar")
    public ResponseEntity<?> invitarUsuario(@Valid @RequestBody UsuarioRegistroDTO dto) {
        try {
            Usuario nuevoUsuario = usuarioService.invitarUsuario(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("id", nuevoUsuario.getId());
            response.put("correo", nuevoUsuario.getUsername());
            response.put("rol", nuevoUsuario.getRol());
            response.put("mensaje", "Usuario registrado exitosamente en la empresa");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody com.edu.javeriana.backend.dto.UsuarioLoginDTO dto) {
        try {
            Usuario usuario = usuarioService.iniciarSesion(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("correo", usuario.getUsername());
            response.put("rol", usuario.getRol());
            response.put("empresa_id", usuario.getEmpresa().getId());
            response.put("mensaje", "Inicio de sesión exitoso");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Se debe retornar UNAUTHORIZED o BAD_REQUEST dependiendo del criterio, 401 es lo habitual para login fallido
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
