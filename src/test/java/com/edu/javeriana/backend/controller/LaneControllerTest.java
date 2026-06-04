package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.service.interfaces.ILaneService;
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
class LaneControllerTest {

    @Mock
    private ILaneService laneService;

    @InjectMocks
    private LaneController laneController;

    @Test
    void crearLane() {
        LaneRegistroDTO dto = new LaneRegistroDTO();
        dto.setNombre("Lane X");
        Mockito.when(laneService.crearLane(any())).thenReturn(dto);

        ResponseEntity<LaneRegistroDTO> response = laneController.crearLane(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Lane X", response.getBody().getNombre());
    }

    @Test
    void editarLane() {
        LaneEdicionDTO dto = new LaneEdicionDTO();
        dto.setNombre("Lane Y");
        Mockito.when(laneService.editarLane(anyLong(), any())).thenReturn(dto);

        ResponseEntity<LaneEdicionDTO> response = laneController.editarLane(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Lane Y", response.getBody().getNombre());
    }

    @Test
    void eliminarLane() {
        ResponseEntity<Void> response = laneController.eliminarLane(1L, 1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(laneService).eliminarLane(1L, 1L);
    }

    @Test
    void listarLanesPorPool() {
        Mockito.when(laneService.listarLanesPorPool(1L, 2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<LaneRegistroDTO>> response = laneController.listarLanesPorPool(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void crearLane_BusinessRuleException_retorna400() {
        Mockito.when(laneService.crearLane(any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<LaneRegistroDTO> response = laneController.crearLane(new LaneRegistroDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void crearLane_NotFound_retorna404() {
        Mockito.when(laneService.crearLane(any())).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<LaneRegistroDTO> response = laneController.crearLane(new LaneRegistroDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editarLane_BusinessRuleException_retorna400() {
        Mockito.when(laneService.editarLane(anyLong(), any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<LaneEdicionDTO> response = laneController.editarLane(1L, new LaneEdicionDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editarLane_NotFound_retorna404() {
        Mockito.when(laneService.editarLane(anyLong(), any())).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<LaneEdicionDTO> response = laneController.editarLane(1L, new LaneEdicionDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void eliminarLane_BusinessRuleException_retorna400() {
        Mockito.doThrow(new BusinessRuleException("error")).when(laneService).eliminarLane(1L, 1L);
        ResponseEntity<Void> response = laneController.eliminarLane(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void eliminarLane_NotFound_retorna404() {
        Mockito.doThrow(new ResourceNotFoundException("no existe")).when(laneService).eliminarLane(1L, 1L);
        ResponseEntity<Void> response = laneController.eliminarLane(1L, 1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarLanes_BusinessRuleException_retorna400() {
        Mockito.when(laneService.listarLanesPorPool(1L, 2L)).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<List<LaneRegistroDTO>> response = laneController.listarLanesPorPool(1L, 2L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listarLanes_NotFound_retorna404() {
        Mockito.when(laneService.listarLanesPorPool(1L, 2L)).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<List<LaneRegistroDTO>> response = laneController.listarLanesPorPool(1L, 2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
