package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.service.interfaces.IPoolService;
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
class PoolControllerTest {

    @Mock
    private IPoolService poolService;

    @InjectMocks
    private PoolController poolController;

    @Test
    void crearPool() {
        PoolRegistroDTO dto = new PoolRegistroDTO();
        dto.setNombre("Pool X");
        Mockito.when(poolService.crearPool(any())).thenReturn(dto);

        ResponseEntity<PoolRegistroDTO> response = poolController.crearPool(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Pool X", response.getBody().getNombre());
    }

    @Test
    void editarPool() {
        PoolEdicionDTO dto = new PoolEdicionDTO();
        dto.setNombre("Pool Y");
        Mockito.when(poolService.editarPool(anyLong(), any())).thenReturn(dto);

        ResponseEntity<PoolEdicionDTO> response = poolController.editarPool(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pool Y", response.getBody().getNombre());
    }

    @Test
    void eliminarPool() {
        ResponseEntity<Void> response = poolController.eliminarPool(1L, 1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(poolService).eliminarPool(1L, 1L);
    }

    @Test
    void listarPoolsPorEmpresa() {
        Mockito.when(poolService.listarPoolsPorEmpresa(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<PoolRegistroDTO>> response = poolController.listarPoolsPorEmpresa(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void crearPool_BusinessRuleException_retorna400() {
        Mockito.when(poolService.crearPool(any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<PoolRegistroDTO> response = poolController.crearPool(new PoolRegistroDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void crearPool_NotFound_retorna404() {
        Mockito.when(poolService.crearPool(any())).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<PoolRegistroDTO> response = poolController.crearPool(new PoolRegistroDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editarPool_BusinessRuleException_retorna400() {
        Mockito.when(poolService.editarPool(anyLong(), any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<PoolEdicionDTO> response = poolController.editarPool(1L, new PoolEdicionDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editarPool_NotFound_retorna404() {
        Mockito.when(poolService.editarPool(anyLong(), any())).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<PoolEdicionDTO> response = poolController.editarPool(1L, new PoolEdicionDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void eliminarPool_BusinessRuleException_retorna400() {
        Mockito.doThrow(new BusinessRuleException("error")).when(poolService).eliminarPool(1L, 1L);
        ResponseEntity<Void> response = poolController.eliminarPool(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void eliminarPool_NotFound_retorna404() {
        Mockito.doThrow(new ResourceNotFoundException("no existe")).when(poolService).eliminarPool(1L, 1L);
        ResponseEntity<Void> response = poolController.eliminarPool(1L, 1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarPools_NotFound_retorna404() {
        Mockito.when(poolService.listarPoolsPorEmpresa(1L)).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<List<PoolRegistroDTO>> response = poolController.listarPoolsPorEmpresa(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
