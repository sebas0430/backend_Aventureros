package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.DocumentoDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Documento;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.repository.DocumentoRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import org.modelmapper.ModelMapper;
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
import java.util.stream.Collectors;

@Service
public class DocumentoService implements IDocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ProcesoRepository procesoRepository;
    private final ModelMapper modelMapper;

    // Directorio donde se guardarán los archivos localmente (simulado local file
    // system)
    private final String UPLOAD_DIR = "uploads/";

    public DocumentoService(DocumentoRepository documentoRepository,
                            ProcesoRepository procesoRepository,
                            ModelMapper modelMapper) {
        this.documentoRepository = documentoRepository;
        this.procesoRepository   = procesoRepository;
        this.modelMapper         = modelMapper;
    }

    @Override
    @Transactional
    public DocumentoDTO subirDocumento(Long procesoId, MultipartFile archivo) {
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

            Documento guardado = documentoRepository.save(documento);

            DocumentoDTO dto = modelMapper.map(guardado, DocumentoDTO.class);
            dto.setProcesoId(guardado.getProceso().getId());
            return dto;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarDocumentosPorProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return documentoRepository.findByProcesoId(procesoId)
                .stream()
                .map(d -> {
                    DocumentoDTO dto = modelMapper.map(d, DocumentoDTO.class);
                    dto.setProcesoId(d.getProceso().getId());
                    return dto;
                })
                .collect(Collectors.toList());
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
            log.info("Documento {} eliminado exitosamente", documentoId);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo físico", e);
        }
    }

@Override
@Transactional
public DocumentoDTO actualizarDocumento(Long documentoId, MultipartFile archivo) {
    Documento documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

    if (archivo.isEmpty()) {
        throw new IllegalArgumentException("El archivo está vacío");
    }

    try {
        // Eliminar el archivo físico anterior
        Path rutaAnterior = Paths.get(documento.getRutaArchivo());
        Files.deleteIfExists(rutaAnterior);

        // Guardar el nuevo archivo
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
        Path filePath = uploadPath.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Actualizar campos
        documento.setNombreArchivo(nombreOriginal);
        documento.setRutaArchivo(filePath.toString());
        documento.setTipoContenido(archivo.getContentType());

        Documento actualizado = documentoRepository.save(documento);

        DocumentoDTO dto = modelMapper.map(actualizado, DocumentoDTO.class);
        dto.setProcesoId(actualizado.getProceso().getId());
        return dto;

    } catch (IOException e) {
        throw new RuntimeException("Error al actualizar el archivo", e);
    }
}
}
