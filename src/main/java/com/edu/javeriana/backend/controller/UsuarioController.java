package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    /**
     * Body esperado: { "correo": "...", "password": "...", "rol": "...", "empresaId": 1 }
     * Retorna UsuarioRegistroDTO (sin contraseña).
     */
    @PostMapping("/invitar")
    public ResponseEntity<UsuarioRegistroDTO> invitarUsuario(@RequestBody Map<String, Object> body) {
        String correo   = (String) body.get("correo");
        String password = (String) body.get("password");
        String rol      = (String) body.get("rol");
        Long empresaId  = Long.valueOf(body.get("empresaId").toString());

        UsuarioRegistroDTO response = usuarioService.invitarUsuario(correo, password, rol, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Body esperado: { "correo": "...", "password": "..." }
     * Retorna UsuarioLoginDTO (sin contraseña).
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginDTO> iniciarSesion(@RequestBody Map<String, String> body) {
        String correo   = body.get("correo");
        String password = body.get("password");

        UsuarioLoginDTO response = usuarioService.iniciarSesion(correo, password);
        return ResponseEntity.ok(response);
    }
}
