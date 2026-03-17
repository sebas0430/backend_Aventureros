package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Documento;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.repository.DocumentoRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentoService implements IDocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProcesoRepository procesoRepository;

    // Directorio donde se guardarán los archivos localmente (simulado local file system)
    private final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional
    public Documento subirDocumento(Long procesoId, MultipartFile archivo) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        try {
            // Asegurarse de que el directorio exista
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar un nombre único para evitar colisiones
            String nombreOriginal = archivo.getOriginalFilename();
            String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
            Path filePath = uploadPath.resolve(nombreUnico);

            // Copiar el archivo al sistema local
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Guardar en base de datos
            Documento documento = new Documento();
            documento.setNombreArchivo(nombreOriginal);
            documento.setRutaArchivo(filePath.toString());
            documento.setTipoContenido(archivo.getContentType());
            documento.setProceso(proceso);

            return documentoRepository.save(documento);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Documento> listarDocumentosPorProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return documentoRepository.findByProcesoId(procesoId);
    }

    @Override
    @Transactional
    public void eliminarDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

        try {
            Path filePath = Paths.get(documento.getRutaArchivo());
            Files.deleteIfExists(filePath);
            documentoRepository.delete(documento);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo físico", e);
        }
    }
}
