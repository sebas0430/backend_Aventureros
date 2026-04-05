package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IRolProcesoService;
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
class RolProcesoControllerTest {

    @Mock
    private IRolProcesoService rolProcesoService;

    @InjectMocks
    private RolProcesoController rolProcesoController;

    @Test
    void crearRolProceso() {
        RolProcesoRegistroDTO dto = new RolProcesoRegistroDTO();
        dto.setNombre("Rol P X");
        Mockito.when(rolProcesoService.crearRolProceso(any())).thenReturn(dto);

        ResponseEntity<RolProcesoRegistroDTO> response = rolProcesoController.crearRolProceso(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Rol P X", response.getBody().getNombre());
    }

    @Test
    void editarRolProceso() {
        RolProcesoEdicionDTO dto = new RolProcesoEdicionDTO();
        dto.setNombre("Rol P Y");
        Mockito.when(rolProcesoService.editarRolProceso(anyLong(), any())).thenReturn(dto);

        ResponseEntity<RolProcesoEdicionDTO> response = rolProcesoController.editarRolProceso(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Rol P Y", response.getBody().getNombre());
    }

    @Test
    void listarRolesPorEmpresa() {
        Mockito.when(rolProcesoService.listarRolesPorEmpresa(1L, 2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<RolProcesoRegistroDTO>> response = rolProcesoController.listarRolesPorEmpresa(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void obtenerRolProceso() {
        RolProcesoRegistroDTO dto = new RolProcesoRegistroDTO();
        Mockito.when(rolProcesoService.obtenerRolProcesoPorId(1L)).thenReturn(dto);
        ResponseEntity<RolProcesoRegistroDTO> response = rolProcesoController.obtenerRolProceso(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarRolProceso() {
        ResponseEntity<Map<String, String>> response = rolProcesoController.eliminarRolProceso(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(rolProcesoService).eliminarRolProceso(1L, 2L);
    }

    @Test
    void consultarRolesConDetalle() {
        Mockito.when(rolProcesoService.consultarRolesConDetalle(1L, 2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<RolProcesoDetalleDTO>> response = rolProcesoController.consultarRolesConDetalle(1L, 2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void consultarRolProcesoDetalle() {
        RolProcesoDetalleDTO dto = new RolProcesoDetalleDTO();
        Mockito.when(rolProcesoService.consultarRolProcesoDetalle(1L)).thenReturn(dto);
        ResponseEntity<RolProcesoDetalleDTO> response = rolProcesoController.consultarRolProcesoDetalle(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
