package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
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
class EmpresaControllerTest {

    @Mock
    private IEmpresaService empresaService;

    @InjectMocks
    private EmpresaController empresaController;

    @Test
    void registrarEmpresa() {
        EmpresaRegistroDTO dto = new EmpresaRegistroDTO();
        dto.setNombre("Empresa X");
        Mockito.when(empresaService.registrarEmpresa(any())).thenReturn(dto);

        ResponseEntity<EmpresaRegistroDTO> response = empresaController.registrarEmpresa(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Empresa X", response.getBody().getNombre());
    }

    @Test
    void editarEmpresa() {
        EmpresaEdicionDTO dto = new EmpresaEdicionDTO();
        dto.setNombre("Empresa Y");
        Mockito.when(empresaService.editarEmpresa(anyLong(), any())).thenReturn(dto);

        ResponseEntity<EmpresaEdicionDTO> response = empresaController.editarEmpresa(1L, dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Empresa Y", response.getBody().getNombre());
    }

    @Test
    void obtenerEmpresa() {
        EmpresaRegistroDTO dto = new EmpresaRegistroDTO();
        Mockito.when(empresaService.obtenerEmpresa(1L)).thenReturn(dto);

        ResponseEntity<EmpresaRegistroDTO> response = empresaController.obtenerEmpresa(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listarEmpresas() {
        Mockito.when(empresaService.listarEmpresas()).thenReturn(Collections.emptyList());

        ResponseEntity<List<EmpresaRegistroDTO>> response = empresaController.listarEmpresas();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarEmpresa() {
        ResponseEntity<Map<String, String>> response = empresaController.eliminarEmpresa(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(empresaService).eliminarEmpresa(1L);
    }
}
