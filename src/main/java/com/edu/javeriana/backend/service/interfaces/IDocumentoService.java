package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.DocumentoDTO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.List;

public interface IDocumentoService {

    DocumentoDTO subirDocumento(Long procesoId, MultipartFile archivo);

    List<DocumentoDTO> listarDocumentosPorProceso(Long procesoId);

    void eliminarDocumento(Long documentoId);

    DocumentoDTO actualizarDocumento(Long documentoId, MultipartFile archivo);

    Resource descargarDocumento(Long documentoId);
}