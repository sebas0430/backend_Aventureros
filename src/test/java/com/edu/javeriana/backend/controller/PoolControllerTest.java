package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
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
}
