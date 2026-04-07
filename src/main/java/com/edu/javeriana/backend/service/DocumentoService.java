package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IDocumentoService;
import com.edu.javeriana.backend.dto.DocumentoDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.exception.FileOperationException;
import com.edu.javeriana.backend.model.Documento;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.repository.DocumentoRepository;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import org.springframework.context.annotation.Lazy;
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


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentoService implements IDocumentoService {

    private final DocumentoRepository documentoRepository;
    private final IProcesoService procesoService;
    private final ModelMapper modelMapper;

    // Directorio donde se guardarán los archivos localmente (simulado local file system)
    private static final String UPLOAD_DIR = "uploads/";

    public DocumentoService(DocumentoRepository documentoRepository,
                            @Lazy IProcesoService procesoService,
                            ModelMapper modelMapper) {
        this.documentoRepository = documentoRepository;
        this.procesoService      = procesoService;
        this.modelMapper         = modelMapper;
    }

    @Override
    @Transactional
    public DocumentoDTO subirDocumento(Long procesoId, MultipartFile archivo) {
        // Buscamos el proceso al que le vamos a colgar el archivo.
        Proceso proceso = procesoService.obtenerProcesoEntity(procesoId);

        // Si mandaron algo vacío, pues no hay nada que guardar.
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        try {
            // Revisamos que la carpeta "uploads/" donde guardamos las cosas exista.
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Le ponemos un ID único al nombre del archivo para que no se sobreescriban si se llaman igual.
            String nombreOriginal = archivo.getOriginalFilename();
            String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
            Path filePath = uploadPath.resolve(nombreUnico);

            // Movemos el archivo de la memoria ram al disco duro (carpeta uploads).
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Guardamos la referencia (el nombre y la ruta) en la base de datos.
            Documento documento = new Documento();
            documento.setNombreArchivo(nombreOriginal);
            documento.setRutaArchivo(filePath.toString());
            documento.setTipoContenido(archivo.getContentType());
            documento.setProceso(proceso);

            Documento guardado = documentoRepository.save(documento);

            // Mapeamos a DTO y listo.
            DocumentoDTO dto = modelMapper.map(guardado, DocumentoDTO.class);
            dto.setProcesoId(guardado.getProceso().getId());
            
            log.info("Documento {} subido exitosamente al proceso {}", nombreOriginal, procesoId);
            
            return dto;

        } catch (IOException e) {
            log.error("Error al guardar el archivo: {}", e.getMessage(), e);
            throw new FileOperationException("Error al guardar el archivo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarDocumentosPorProceso(Long procesoId) {
        if (!procesoService.existeProceso(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return documentoRepository.findByProcesoId(procesoId)
                .stream()
                .map(d -> {
                    DocumentoDTO dto = modelMapper.map(d, DocumentoDTO.class);
                    dto.setProcesoId(d.getProceso().getId());
                    return dto;
                })
                .toList();
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
            log.error("Error al eliminar el archivo físico: {}", e.getMessage(), e);
            throw new FileOperationException("Error al eliminar el archivo físico", e);
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
        
        log.info("Documento {} actualizado exitosamente con nuevo archivo {}", documentoId, nombreOriginal);
        
        return dto;

    } catch (IOException e) {
        log.error("Error al actualizar el archivo: {}", e.getMessage(), e);
        throw new FileOperationException("Error al actualizar el archivo", e);
    }
}
}
