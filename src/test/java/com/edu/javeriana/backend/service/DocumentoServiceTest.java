package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.DocumentoDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Documento;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.repository.DocumentoRepository;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private IProcesoService procesoService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DocumentoService documentoService;

    private Proceso proceso;
    private Documento documento;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);

        documento = new Documento();
        documento.setId(1L);
        documento.setNombreArchivo("test.txt");
        documento.setRutaArchivo("uploads/test.txt");
        documento.setProceso(proceso);
    }

    @Test
    void subirDocumento_Exitoso() throws IOException {
        MockMultipartFile file = new MockMultipartFile("archivo", "test.txt", "text/plain", "content".getBytes());
        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(modelMapper.map(any(), eq(DocumentoDTO.class))).thenReturn(new DocumentoDTO());

        DocumentoDTO res = documentoService.subirDocumento(1L, file);

        assertNotNull(res);
        verify(documentoRepository).save(any());
    }

    @Test
    void listarDocumentosPorProceso_Exitoso() {
        when(procesoService.existeProceso(1L)).thenReturn(true);
        when(documentoRepository.findByProcesoId(1L)).thenReturn(List.of(documento));
        when(modelMapper.map(any(), eq(DocumentoDTO.class))).thenReturn(new DocumentoDTO());

        List<DocumentoDTO> list = documentoService.listarDocumentosPorProceso(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void eliminarDocumento_Exitoso() {
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        // Note: Files.deleteIfExists will try to delete the file. In a real environment, this might fail or pass.
        // For unit testing logic, we assume we want to verify the repository call.
        documentoService.eliminarDocumento(1L);
        verify(documentoRepository).delete(documento);
    }

    @Test
    void actualizarDocumento_Exitoso() {
        MockMultipartFile file = new MockMultipartFile("archivo", "new.txt", "text/plain", "new content".getBytes());
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(documentoRepository.save(any())).thenReturn(documento);
        when(modelMapper.map(any(), eq(DocumentoDTO.class))).thenReturn(new DocumentoDTO());

        DocumentoDTO res = documentoService.actualizarDocumento(1L, file);
        assertNotNull(res);
    }
}
