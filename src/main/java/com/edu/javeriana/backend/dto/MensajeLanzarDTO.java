package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MensajeLanzarDTO {

    @NotNull(message = "El ID del evento de origen es obligatorio")
    private Long eventoOrigenId;

    private String payload; // JSON con los datos

    @NotNull(message = "El ID del usuario ejecutor es obligatorio")
    private Long usuarioId;
}
