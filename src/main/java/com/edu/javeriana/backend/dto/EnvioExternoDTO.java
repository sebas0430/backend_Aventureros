package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnvioExternoDTO {

    @NotNull(message = "El ID del conector externo es obligatorio")
    private Long conectorId;

    @NotNull(message = "El ID del proceso emisor es obligatorio")
    private Long procesoId;

    /** Payload JSON con variables del proceso mapeadas al formato del destino */
    private String payload;

    @NotNull(message = "El ID del usuario ejecutor es obligatorio")
    private Long usuarioId;
}
