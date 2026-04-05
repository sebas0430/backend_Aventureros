package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IGatewayService;
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
class GatewayControllerTest {

    @Mock
    private IGatewayService gatewayService;

    @InjectMocks
    private GatewayController gatewayController;

    @Test
    void crearGateway() {
        GatewayRegistroDTO dto = new GatewayRegistroDTO();
        dto.setNombre("Gateway X");
        Mockito.when(gatewayService.crearGateway(any())).thenReturn(dto);

        ResponseEntity<GatewayRegistroDTO> response = gatewayController.crearGateway(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Gateway X", response.getBody().getNombre());
    }

    @Test
    void editarGateway() {
        GatewayEdicionDTO dto = new GatewayEdicionDTO();
        dto.setNombre("Gateway Y");
        Mockito.when(gatewayService.editarGateway(anyLong(), any())).thenReturn(dto);

        ResponseEntity<GatewayEdicionDTO> response = gatewayController.editarGateway(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Gateway Y", response.getBody().getNombre());
    }

    @Test
    void listarGatewaysPorProceso() {
        Mockito.when(gatewayService.listarGatewaysPorProceso(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<GatewayRegistroDTO>> response = gatewayController.listarGatewaysPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerGateway() {
        GatewayRegistroDTO dto = new GatewayRegistroDTO();
        Mockito.when(gatewayService.obtenerGateway(1L)).thenReturn(dto);
        ResponseEntity<GatewayRegistroDTO> response = gatewayController.obtenerGateway(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarGateway() {
        ResponseEntity<Map<String, String>> response = gatewayController.eliminarGateway(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(gatewayService).eliminarGateway(1L, 1L);
    }
}
