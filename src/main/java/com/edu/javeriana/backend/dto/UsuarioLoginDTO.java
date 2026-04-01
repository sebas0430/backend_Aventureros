package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para inicio de sesión y respuesta de autenticación.
 * No contiene contraseña; es seguro usarlo como respuesta del servicio y controlador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLoginDTO {

    private Long id;

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    private String rol;
    private Boolean activo;
    private Long empresaId;
}
