package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.model.Documento;
import com.edu.javeriana.backend.service.IDocumentoService;
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

    private final IDocumentoService documentoService;

    @PostMapping("/proceso/{procesoId}")
    public ResponseEntity<?> subirDocumento(
            @PathVariable Long procesoId,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            Documento doc = documentoService.subirDocumento(procesoId, archivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", doc.getId(),
                    "nombreArchivo", doc.getNombreArchivo(),
                    "mensaje", "Documento subido exitosamente"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<Documento>> listarDocumentos(@PathVariable Long procesoId) {
        return ResponseEntity.ok(documentoService.listarDocumentosPorProceso(procesoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDocumento(@PathVariable Long id) {
        documentoService.eliminarDocumento(id);
        return ResponseEntity.ok(Map.of("mensaje", "Documento eliminado exitosamente"));
    }
}
