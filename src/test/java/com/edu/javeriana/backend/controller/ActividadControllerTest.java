package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IActividadService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

    @Mock
    private IActividadService actividadService;

    @InjectMocks
    private ActividadController actividadController;

    @Test
    void crearActividad() {
        ActividadRegistroDTO dto = new ActividadRegistroDTO();
        dto.setNombre("Actividad X");
        Mockito.when(actividadService.crearActividad(any())).thenReturn(dto);

        ResponseEntity<ActividadRegistroDTO> response = actividadController.crearActividad(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Actividad X", response.getBody().getNombre());
    }

    @Test
    void editarActividad() {
        ActividadEdicionDTO dto = new ActividadEdicionDTO();
        dto.setNombre("Actividad Y");
        Mockito.when(actividadService.editarActividad(anyLong(), any())).thenReturn(dto);

        ResponseEntity<ActividadEdicionDTO> response = actividadController.editarActividad(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Actividad Y", response.getBody().getNombre());
    }

    @Test
    void eliminarActividad() {
        ResponseEntity<Map<String, String>> response = actividadController.eliminarActividad(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(actividadService).eliminarActividad(1L, 1L);
    }

    @Test
    void listarPorProceso() {
        Mockito.when(actividadService.listarPorProceso(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<ActividadRegistroDTO>> response = actividadController.listarPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerActividad() {
        ActividadRegistroDTO dto = new ActividadRegistroDTO();
        Mockito.when(actividadService.obtenerPorId(1L)).thenReturn(dto);
        ResponseEntity<ActividadRegistroDTO> response = actividadController.obtenerActividad(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
