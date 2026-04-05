package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.service.interfaces.IMessageCatchService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class MessageCatchControllerTest {

    @Mock
    private IMessageCatchService messageCatchService;

    @InjectMocks
    private MessageCatchController messageCatchController;

    @Test
    void recibirMensaje() {
        MensajeCatchDTO dto = new MensajeCatchDTO();
        Mockito.when(messageCatchService.recibirMensaje(any())).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<MensajeCatchDTO>> response = messageCatchController.recibirMensaje(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void logsPorProceso() {
        Mockito.when(messageCatchService.listarRecepcionesPorProceso(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MensajeCatchDTO>> response = messageCatchController.logsPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void logsPorCatch() {
        Mockito.when(messageCatchService.listarRecepcionesPorCatch(anyLong())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MensajeCatchDTO>> response = messageCatchController.logsPorCatch(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
