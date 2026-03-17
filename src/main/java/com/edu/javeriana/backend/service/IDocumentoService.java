package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.model.Documento;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentoService {
    Documento subirDocumento(Long procesoId, MultipartFile archivo);
    List<Documento> listarDocumentosPorProceso(Long procesoId);
    void eliminarDocumento(Long documentoId);
}
