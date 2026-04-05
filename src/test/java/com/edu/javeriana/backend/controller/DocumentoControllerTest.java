package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.DocumentoDTO;
import com.edu.javeriana.backend.service.interfaces.IDocumentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class DocumentoControllerTest {

    @Mock
    private IDocumentoService documentoService;

    @InjectMocks
    private DocumentoController documentoController;

    @Test
    void subirDocumento() {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombreArchivo("test.txt");
        Mockito.when(documentoService.subirDocumento(anyLong(), any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("archivo", "test.txt", "text/plain", "content".getBytes());
        ResponseEntity<DocumentoDTO> response = documentoController.subirDocumento(1L, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("test.txt", response.getBody().getNombreArchivo());
    }

    @Test
    void actualizarDocumento() {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setNombreArchivo("test2.txt");
        Mockito.when(documentoService.actualizarDocumento(anyLong(), any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("archivo", "test2.txt", "text/plain", "content".getBytes());
        ResponseEntity<DocumentoDTO> response = documentoController.actualizarDocumento(1L, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test2.txt", response.getBody().getNombreArchivo());
    }

    @Test
    void listarDocumentos() {
        Mockito.when(documentoService.listarDocumentosPorProceso(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DocumentoDTO>> response = documentoController.listarDocumentos(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void eliminarDocumento() {
        ResponseEntity<Map<String, String>> response = documentoController.eliminarDocumento(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(documentoService).eliminarDocumento(1L);
    }
}
