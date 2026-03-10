package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcesoRegistroDTO {

    @NotBlank(message = "El título del proceso es obligatorio")
    private String titulo;

    private String definicionJson;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del autor es obligatorio")
    private Long autorId;
}
