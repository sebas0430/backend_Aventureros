package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IArcoService;
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
class ArcoControllerTest {

    @Mock
    private IArcoService arcoService;

    @InjectMocks
    private ArcoController arcoController;

    @Test
    void crearArco() {
        ArcoRegistroDTO dto = new ArcoRegistroDTO();
        dto.setEtiqueta("Arco X");
        Mockito.when(arcoService.crearArco(any())).thenReturn(dto);

        ResponseEntity<ArcoRegistroDTO> response = arcoController.crearArco(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Arco X", response.getBody().getEtiqueta());
    }

    @Test
    void editarArco() {
        ArcoEdicionDTO dto = new ArcoEdicionDTO();
        dto.setEtiqueta("Arco Y");
        Mockito.when(arcoService.editarArco(anyLong(), any())).thenReturn(dto);

        ResponseEntity<ArcoEdicionDTO> response = arcoController.editarArco(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Arco Y", response.getBody().getEtiqueta());
    }

    @Test
    void listarArcosPorProceso() {
        Mockito.when(arcoService.listarArcosPorProceso(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<ArcoRegistroDTO>> response = arcoController.listarArcosPorProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerArco() {
        ArcoRegistroDTO dto = new ArcoRegistroDTO();
        Mockito.when(arcoService.obtenerArcoPorId(1L)).thenReturn(dto);
        ResponseEntity<ArcoRegistroDTO> response = arcoController.obtenerArco(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarArco() {
        ResponseEntity<Map<String, String>> response = arcoController.eliminarArco(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(arcoService).eliminarArco(1L, 1L);
    }

    @Test
    void eliminarArcosPorProceso() {
        ResponseEntity<Map<String, String>> response = arcoController.eliminarArcosPorProceso(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(arcoService).eliminarArcosPorProceso(1L, 1L);
    }
}
