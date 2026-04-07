package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    // Registra a alguien nuevo en el sistema (el admin manda la invitación).
    @PostMapping("/invitar")
    public ResponseEntity<UsuarioRegistroDTO> invitarUsuario(@RequestBody Map<String, Object> body) {
        String correo   = (String) body.get("correo");
        String password = (String) body.get("password");
        String rol      = (String) body.get("rol");
        Long empresaId  = Long.valueOf(body.get("empresaId").toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.invitarUsuario(correo, password, rol, empresaId));
    }

    // El clásico login: checa el correo y la contraseña cifrada.
    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginDTO> iniciarSesion(@RequestBody Map<String, String> body) {
        String correo   = body.get("correo");
        String password = body.get("password");

        return ResponseEntity.ok(usuarioService.iniciarSesion(correo, password));
    }

    // Obtener los datos de un usuario por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRegistroDTO> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuario(id));
    }

    // Listar a todos los que trabajan en la misma empresa.
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<UsuarioRegistroDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(empresaId));
    }

    // Para cambiar el rol de alguien o activarlo/desactivarlo.
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRegistroDTO> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String rol     = (String) body.get("rol");
        Boolean activo = body.get("activo") != null ? (Boolean) body.get("activo") : null;

        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, rol, activo));
    }

    // Borrar a un usuario definitivamente.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente"));
    }
}