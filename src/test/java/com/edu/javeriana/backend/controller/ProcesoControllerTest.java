package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.EstadoProceso;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProcesoControllerTest {

    @Mock
    private IProcesoService procesoService;

    @InjectMocks
    private ProcesoController procesoController;

    @Test
    void crearProceso() {
        ProcesoRegistroDTO dto = new ProcesoRegistroDTO();
        dto.setNombre("Proceso X");
        Mockito.when(procesoService.crearProceso(any())).thenReturn(dto);

        ResponseEntity<?> response = procesoController.crearProceso(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Proceso X", ((ProcesoRegistroDTO) response.getBody()).getNombre());
    }

    @Test
    void listarPorEmpresa() {
        Mockito.when(procesoService.listarPorEmpresa(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarPorEmpresa(1L, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarPorEmpresaConFiltros() {
        Mockito.when(procesoService.filtrarProcesos(eq(1L), anyString(), anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarPorEmpresa(1L, "ACTIVO", "CAT");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerProceso() {
        ProcesoRegistroDTO dto = new ProcesoRegistroDTO();
        Mockito.when(procesoService.obtenerProcesoPorId(1L)).thenReturn(dto);
        ResponseEntity<ProcesoRegistroDTO> response = procesoController.obtenerProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarPorAutor() {
        Mockito.when(procesoService.listarPorAutor(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarPorAutor(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void actualizarDefinicion() {
        ProcesoEdicionDTO res = new ProcesoEdicionDTO();
        Mockito.when(procesoService.actualizarDefinicion(anyLong(), anyString())).thenReturn(res);

        Map<String, String> body = new HashMap<>();
        body.put("definicionJson", "{}");

        ResponseEntity<?> response = procesoController.actualizarDefinicion(1L, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editarProceso() {
        ProcesoEdicionDTO dto = new ProcesoEdicionDTO();
        Mockito.when(procesoService.editarProceso(anyLong(), any())).thenReturn(dto);

        ResponseEntity<ProcesoEdicionDTO> response = procesoController.editarProceso(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarProceso() {
        ResponseEntity<Void> response = procesoController.eliminarProceso(1L, 1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(procesoService).eliminarProceso(1L, 1L);
    }

    @Test
    void cambiarEstado() {
        ProcesoEdicionDTO res = new ProcesoEdicionDTO();
        Mockito.when(procesoService.cambiarEstado(anyLong(), any(EstadoProceso.class), anyLong())).thenReturn(res);

        Map<String, Object> body = new HashMap<>();
        body.put("estado", "PUBLICADO");
        body.put("usuarioId", 1L);

        ResponseEntity<ProcesoEdicionDTO> response = procesoController.cambiarEstado(1L, body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void compartirProceso() {
        ProcesoCompartirDTO dto = new ProcesoCompartirDTO();
        ResponseEntity<ProcesoCompartirDTO> response = procesoController.compartirProceso(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(procesoService).compartirProceso(1L, dto);
    }

    @Test
    void quitarComparticionProceso() {
        ResponseEntity<Void> response = procesoController.quitarComparticionProceso(1L, 2L, 3L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(procesoService).quitarComparticionProceso(1L, 2L, 3L);
    }

    @Test
    void listarProcesosCompartidosConPool() {
        Mockito.when(procesoService.listarProcesosCompartidosConPool(1L, 2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarProcesosCompartidosConPool(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarProcesosPorUsuario() {
        Mockito.when(procesoService.listarProcesosPorUsuario(1L, 1L, null)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = procesoController.listarProcesosPorUsuario(1L, 1L, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ── Error paths ──────────────────────────────────────────────────────────

    @Test
    void crearProceso_BusinessRuleException_retorna400() {
        Mockito.when(procesoService.crearProceso(any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<?> response = (ResponseEntity<?>) procesoController.crearProceso(new ProcesoRegistroDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void crearProceso_ExcepcionGenerica_retorna500() {
        Mockito.when(procesoService.crearProceso(any())).thenThrow(new RuntimeException("fallo"));
        ResponseEntity<?> response = (ResponseEntity<?>) procesoController.crearProceso(new ProcesoRegistroDTO());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void listarPorEmpresa_IllegalArgument_retorna400() {
        Mockito.when(procesoService.filtrarProcesos(eq(1L), anyString(), any()))
                .thenThrow(new IllegalArgumentException("estado inválido"));
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarPorEmpresa(1L, "INVALIDO", null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void obtenerProceso_NotFound_retorna404() {
        Mockito.when(procesoService.obtenerProcesoPorId(1L)).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<ProcesoRegistroDTO> response = procesoController.obtenerProceso(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void actualizarDefinicion_SinCampo_retorna400() {
        Map<String, String> body = new HashMap<>();
        ResponseEntity<?> response = (ResponseEntity<?>) procesoController.actualizarDefinicion(1L, body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void actualizarDefinicion_IllegalArgument_retorna400() {
        Mockito.when(procesoService.actualizarDefinicion(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("error"));
        Map<String, String> body = new HashMap<>();
        body.put("definicionJson", "{}");
        ResponseEntity<?> response = (ResponseEntity<?>) procesoController.actualizarDefinicion(1L, body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editarProceso_BusinessRuleException_retorna400() {
        Mockito.when(procesoService.editarProceso(anyLong(), any())).thenThrow(new BusinessRuleException("error"));
        ResponseEntity<ProcesoEdicionDTO> response = procesoController.editarProceso(1L, new ProcesoEdicionDTO());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editarProceso_NotFound_retorna404() {
        Mockito.when(procesoService.editarProceso(anyLong(), any())).thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<ProcesoEdicionDTO> response = procesoController.editarProceso(1L, new ProcesoEdicionDTO());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void eliminarProceso_BusinessRuleException_retorna400() {
        Mockito.doThrow(new BusinessRuleException("error")).when(procesoService).eliminarProceso(1L, 1L);
        ResponseEntity<Void> response = procesoController.eliminarProceso(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void eliminarProceso_NotFound_retorna404() {
        Mockito.doThrow(new ResourceNotFoundException("no existe")).when(procesoService).eliminarProceso(1L, 1L);
        ResponseEntity<Void> response = procesoController.eliminarProceso(1L, 1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void cambiarEstado_BusinessRuleException_retorna400() {
        Mockito.when(procesoService.cambiarEstado(anyLong(), any(), anyLong()))
                .thenThrow(new BusinessRuleException("error"));
        Map<String, Object> body = new HashMap<>();
        body.put("estado", "PUBLICADO");
        body.put("usuarioId", 1L);
        ResponseEntity<ProcesoEdicionDTO> response = procesoController.cambiarEstado(1L, body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void cambiarEstado_NotFound_retorna404() {
        Mockito.when(procesoService.cambiarEstado(anyLong(), any(), anyLong()))
                .thenThrow(new ResourceNotFoundException("no existe"));
        Map<String, Object> body = new HashMap<>();
        body.put("estado", "PUBLICADO");
        body.put("usuarioId", 1L);
        ResponseEntity<ProcesoEdicionDTO> response = procesoController.cambiarEstado(1L, body);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void compartirProceso_BusinessRuleException_retorna400() {
        ProcesoCompartirDTO dto = new ProcesoCompartirDTO();
        Mockito.doThrow(new BusinessRuleException("error")).when(procesoService).compartirProceso(1L, dto);
        ResponseEntity<ProcesoCompartirDTO> response = procesoController.compartirProceso(1L, dto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void compartirProceso_NotFound_retorna404() {
        ProcesoCompartirDTO dto = new ProcesoCompartirDTO();
        Mockito.doThrow(new ResourceNotFoundException("no existe")).when(procesoService).compartirProceso(1L, dto);
        ResponseEntity<ProcesoCompartirDTO> response = procesoController.compartirProceso(1L, dto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void quitarComparticion_BusinessRuleException_retorna400() {
        Mockito.doThrow(new BusinessRuleException("error")).when(procesoService).quitarComparticionProceso(1L, 2L, 3L);
        ResponseEntity<Void> response = procesoController.quitarComparticionProceso(1L, 2L, 3L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void quitarComparticion_NotFound_retorna404() {
        Mockito.doThrow(new ResourceNotFoundException("no existe")).when(procesoService).quitarComparticionProceso(1L, 2L, 3L);
        ResponseEntity<Void> response = procesoController.quitarComparticionProceso(1L, 2L, 3L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarCompartidosConPool_BusinessRuleException_retorna400() {
        Mockito.when(procesoService.listarProcesosCompartidosConPool(1L, 2L))
                .thenThrow(new BusinessRuleException("error"));
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarProcesosCompartidosConPool(1L, 2L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listarCompartidosConPool_NotFound_retorna404() {
        Mockito.when(procesoService.listarProcesosCompartidosConPool(1L, 2L))
                .thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<List<ProcesoRegistroDTO>> response = procesoController.listarProcesosCompartidosConPool(1L, 2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listarProcesosPorUsuario_BusinessRuleException_retorna400() {
        Mockito.when(procesoService.listarProcesosPorUsuario(1L, 1L, null))
                .thenThrow(new BusinessRuleException("error"));
        ResponseEntity<?> response = procesoController.listarProcesosPorUsuario(1L, 1L, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listarProcesosPorUsuario_NotFound_retorna404() {
        Mockito.when(procesoService.listarProcesosPorUsuario(1L, 1L, null))
                .thenThrow(new ResourceNotFoundException("no existe"));
        ResponseEntity<?> response = procesoController.listarProcesosPorUsuario(1L, 1L, null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
