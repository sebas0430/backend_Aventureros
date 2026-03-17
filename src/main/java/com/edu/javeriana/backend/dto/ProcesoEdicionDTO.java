package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcesoEdicionDTO {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción del proceso es obligatoria")
    private String descripcion;

    @NotBlank(message = "La categoría del proceso es obligatoria")
    private String categoria;

    @NotNull(message = "El ID del usuario que edita es obligatorio")
    private Long usuarioId;
}