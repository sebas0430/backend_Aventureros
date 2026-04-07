package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.DocumentoDTO;
import com.edu.javeriana.backend.service.interfaces.IDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {
 //controlador para manejar las operaciones relacionadas con los documentos, como subir, actualizar, listar y eliminar documentos asociados a procesos.
    private final IDocumentoService documentoService;

    // Sirve para subir un archivo nuevo y pegarlo a un proceso.
    @PostMapping("/proceso/{procesoId}")
    public ResponseEntity<DocumentoDTO> subirDocumento(
            @PathVariable Long procesoId,
            @RequestParam("archivo") MultipartFile archivo) {

        DocumentoDTO doc = documentoService.subirDocumento(procesoId, archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    // Por si quieres cambiar el archivo por una versión más nueva.
    @PutMapping("/{id}")
    public ResponseEntity<DocumentoDTO> actualizarDocumento(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        DocumentoDTO doc = documentoService.actualizarDocumento(id, archivo);
        return ResponseEntity.ok(doc);
    }

    // Lista todos los papeles que se han subido para un proceso.
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<DocumentoDTO>> listarDocumentos(@PathVariable Long procesoId) {
        return ResponseEntity.ok(documentoService.listarDocumentosPorProceso(procesoId));
    }

    // Borra el documento del sistema.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarDocumento(@PathVariable Long id) {
        documentoService.eliminarDocumento(id);
        return ResponseEntity.ok(Map.of("mensaje", "Documento eliminado exitosamente"));
    }
}
