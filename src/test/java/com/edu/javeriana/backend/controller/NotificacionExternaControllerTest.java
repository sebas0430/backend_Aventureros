package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.dto.NotificacionExternaDTO;
import com.edu.javeriana.backend.service.interfaces.INotificacionExternaService;
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
class NotificacionExternaControllerTest {

    @Mock
    private INotificacionExternaService notificacionExternaService;

    @InjectMocks
    private NotificacionExternaController notificacionExternaController;

    @Test
    void crearConector() {
        ConectorExternoRegistroDTO dto = new ConectorExternoRegistroDTO();
        dto.setNombre("Conector X");
        Mockito.when(notificacionExternaService.crearConector(any())).thenReturn(dto);

        ResponseEntity<ConectorExternoRegistroDTO> response = notificacionExternaController.crearConector(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void editarConector() {
        ConectorExternoRegistroDTO dto = new ConectorExternoRegistroDTO();
        Mockito.when(notificacionExternaService.editarConector(anyLong(), any())).thenReturn(dto);

        ResponseEntity<ConectorExternoRegistroDTO> response = notificacionExternaController.editarConector(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarConector() {
        ResponseEntity<Map<String, String>> response = notificacionExternaController.eliminarConector(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(notificacionExternaService).eliminarConector(1L, 2L);
    }

    @Test
    void listarConectores() {
        Mockito.when(notificacionExternaService.listarConectoresPorEmpresa(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<ConectorExternoRegistroDTO>> response = notificacionExternaController.listarConectores(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void enviarMensaje() {
        EnvioExternoDTO dto = new EnvioExternoDTO();
        NotificacionExternaDTO res = new NotificacionExternaDTO();
        Mockito.when(notificacionExternaService.enviarMensajeExterno(any())).thenReturn(res);

        ResponseEntity<NotificacionExternaDTO> response = notificacionExternaController.enviarMensaje(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void logsPorProceso() {
        Mockito.when(notificacionExternaService.listarLogsPorProceso(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<NotificacionExternaDTO>> response = notificacionExternaController.logsPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void logsPorConector() {
        Mockito.when(notificacionExternaService.listarLogsPorConector(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<NotificacionExternaDTO>> response = notificacionExternaController.logsPorConector(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
