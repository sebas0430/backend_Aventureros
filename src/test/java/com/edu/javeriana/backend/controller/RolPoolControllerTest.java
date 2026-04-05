package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IRolPoolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class RolPoolControllerTest {

    @Mock
    private IRolPoolService rolPoolService;

    @InjectMocks
    private RolPoolController rolPoolController;

    @Test
    void crearRol() {
        RolPoolRegistroDTO dto = new RolPoolRegistroDTO();
        dto.setNombre("Rol X");
        Mockito.when(rolPoolService.crearRol(any())).thenReturn(dto);

        ResponseEntity<RolPoolRegistroDTO> response = rolPoolController.crearRol(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Rol X", response.getBody().getNombre());
    }

    @Test
    void editarRol() {
        RolPoolRegistroDTO dto = new RolPoolRegistroDTO();
        dto.setNombre("Rol Y");
        Mockito.when(rolPoolService.editarRol(anyLong(), any())).thenReturn(dto);

        ResponseEntity<RolPoolRegistroDTO> response = rolPoolController.editarRol(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Rol Y", response.getBody().getNombre());
    }

    @Test
    void eliminarRol() {
        ResponseEntity<Map<String, String>> response = rolPoolController.eliminarRol(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(rolPoolService).eliminarRol(1L, 2L);
    }

    @Test
    void listarRolesPorPool() {
        Mockito.when(rolPoolService.listarRolesPorPool(1L, 2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<RolPoolRegistroDTO>> response = rolPoolController.listarRolesPorPool(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void asignarRolAUsuario() {
        AsignacionRolDTO dto = new AsignacionRolDTO();
        Mockito.when(rolPoolService.asignarRolAUsuario(any())).thenReturn(dto);

        ResponseEntity<AsignacionRolDTO> response = rolPoolController.asignarRolAUsuario(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void desasignarRol() {
        ResponseEntity<Map<String, String>> response = rolPoolController.desasignarRol(1L, 2L, 3L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(rolPoolService).desasignarRolAUsuario(1L, 2L, 3L);
    }

    @Test
    void obtenerRolDeUsuario() {
        AsignacionRolDTO dto = new AsignacionRolDTO();
        Mockito.when(rolPoolService.obtenerAsignacionUsuario(1L, 2L)).thenReturn(dto);

        ResponseEntity<AsignacionRolDTO> response = rolPoolController.obtenerRolDeUsuario(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerRolDeUsuario_NoEncontrado() {
        Mockito.when(rolPoolService.obtenerAsignacionUsuario(1L, 2L)).thenReturn(null);

        ResponseEntity<AsignacionRolDTO> response = rolPoolController.obtenerRolDeUsuario(1L, 2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
