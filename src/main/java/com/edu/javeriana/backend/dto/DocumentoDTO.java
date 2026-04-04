package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentoDTO {

    private Long id;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    private String nombreArchivo;

    @NotBlank(message = "La ruta del archivo es obligatoria")
    private String rutaArchivo;

    @NotBlank(message = "El tipo de contenido es obligatorio")
    private String tipoContenido;

    @NotNull(message = "El ID del proceso es obligatorio")
    private Long procesoId;

    private LocalDateTime createdAt;
}
