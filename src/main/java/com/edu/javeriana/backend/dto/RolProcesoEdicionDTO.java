package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolProcesoEdicionDTO {

    @NotBlank(message = "El nombre del rol de proceso es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El ID del usuario solicitante es obligatorio para verificar permisos")
    private Long usuarioId;
}
