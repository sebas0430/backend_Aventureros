package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro/invitación de usuario y respuesta del servicio.
 * No contiene contraseña; es seguro usarlo como respuesta del servicio y controlador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDTO {

    private Long id;

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    private Boolean activo;
}
