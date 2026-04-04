package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RolProcesoRegistroDTO {

    private Long id;

    @NotBlank(message = "El nombre del rol de proceso es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del usuario solicitante es obligatorio para verificar permisos")
    private Long usuarioId;

    private LocalDateTime createdAt;
}