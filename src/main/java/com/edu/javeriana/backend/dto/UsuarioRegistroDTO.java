package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRegistroDTO {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String passwordHash;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId; // Temporary until JWT context is implemented
}
