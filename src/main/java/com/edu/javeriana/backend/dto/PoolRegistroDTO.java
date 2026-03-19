package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PoolRegistroDTO {

    @NotBlank(message = "El nombre del pool es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del usuario administrador que crea el pool es obligatorio")
    private Long usuarioId; // Para validar permisos de rol
}
