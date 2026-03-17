package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GatewayEdicionDTO {

    @NotBlank(message = "El nombre del gateway es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de gateway es obligatorio (EXCLUSIVO, PARALELO, INCLUSIVO)")
    private String tipo;

    @NotNull(message = "El ID del usuario que edita el gateway es obligatorio")
    private Long usuarioId;
}
