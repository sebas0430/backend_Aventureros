package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
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
}
