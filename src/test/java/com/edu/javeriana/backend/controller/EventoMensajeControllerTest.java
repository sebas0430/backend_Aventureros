package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeEjecucionDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.service.interfaces.IEventoMensajeService;
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
class EventoMensajeControllerTest {

    @Mock
    private IEventoMensajeService eventoMensajeService;

    @InjectMocks
    private EventoMensajeController eventoMensajeController;

    @Test
    void crearEvento() {
        EventoMensajeRegistroDTO dto = new EventoMensajeRegistroDTO();
        dto.setNombre("Evento M X");
        Mockito.when(eventoMensajeService.crearEvento(any())).thenReturn(dto);

        ResponseEntity<EventoMensajeRegistroDTO> response = eventoMensajeController.crearEvento(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Evento M X", response.getBody().getNombre());
    }

    @Test
    void listarPorProceso() {
        Mockito.when(eventoMensajeService.listarPorProceso(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<EventoMensajeRegistroDTO>> response = eventoMensajeController.listarPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarEvento() {
        ResponseEntity<Map<String, String>> response = eventoMensajeController.eliminarEvento(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(eventoMensajeService).eliminarEvento(1L, 2L);
    }

    @Test
    void lanzarMensajeThrow() {
        MensajeLanzarDTO dto = new MensajeLanzarDTO();
        Mockito.when(eventoMensajeService.lanzarMensaje(any())).thenReturn(Collections.emptyList());

        ResponseEntity<List<MensajeEjecucionDTO>> response = eventoMensajeController.lanzarMensajeThrow(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarLogsDeLanzamiento() {
        Mockito.when(eventoMensajeService.listarHistorialPorEventoOrigen(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MensajeEjecucionDTO>> response = eventoMensajeController.listarLogsDeLanzamiento(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
