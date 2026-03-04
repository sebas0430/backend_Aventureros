package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.RolGlobal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRegistroDTO {
    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private RolGlobal rolGlobal;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId; // Temporary until JWT context is implemented
}
