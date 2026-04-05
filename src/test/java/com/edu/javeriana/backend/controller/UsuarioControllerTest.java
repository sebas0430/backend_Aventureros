package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void invitarUsuario() {
        Map<String, Object> body = new HashMap<>();
        body.put("correo", "test@test.com");
        body.put("password", "pass");
        body.put("rol", "EDITOR");
        body.put("empresaId", 1L);

        UsuarioRegistroDTO res = new UsuarioRegistroDTO();
        res.setCorreo("test@test.com");
        Mockito.when(usuarioService.invitarUsuario(anyString(), anyString(), anyString(), anyLong())).thenReturn(res);

        ResponseEntity<UsuarioRegistroDTO> response = usuarioController.invitarUsuario(body);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("test@test.com", response.getBody().getCorreo());
    }

    @Test
    void iniciarSesion() {
        Map<String, String> body = new HashMap<>();
        body.put("correo", "test@test.com");
        body.put("password", "pass");

        UsuarioLoginDTO dto = new UsuarioLoginDTO();
        dto.setCorreo("test@test.com");
        dto.setToken("my-token");

        Mockito.when(usuarioService.iniciarSesion("test@test.com", "pass")).thenReturn(dto);

        ResponseEntity<UsuarioLoginDTO> response = usuarioController.iniciarSesion(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("my-token", response.getBody().getToken());
    }

    @Test
    void actualizarUsuario() {
        Map<String, Object> body = new HashMap<>();
        body.put("rol", "ADMINISTRADOR_EMPRESA");
        body.put("activo", true);

        UsuarioRegistroDTO res = new UsuarioRegistroDTO();
        res.setCorreo("new@test.com");

        Mockito.when(usuarioService.actualizarUsuario(anyLong(), anyString(), any())).thenReturn(res);
        ResponseEntity<UsuarioRegistroDTO> response = usuarioController.actualizarUsuario(1L, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarPorEmpresa() {
        Mockito.when(usuarioService.listarPorEmpresa(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<UsuarioRegistroDTO>> response = usuarioController.listarPorEmpresa(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarUsuario() {
        ResponseEntity<Map<String, String>> response = usuarioController.eliminarUsuario(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(usuarioService).eliminarUsuario(1L);
    }
}
