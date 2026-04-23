package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.EstadoProceso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcesoRegistroDTO {

    private Long id; // ID generado tras guardar

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción del proceso es obligatoria")
    private String descripcion;

    @NotBlank(message = "La categoría del proceso es obligatoria")
    private String categoria;

    private EstadoProceso estado;

    private String definicionJson;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del autor es obligatorio")
    private Long autorId;

    private Long poolId; // Opcional: Si no se manda, irá al pool por defecto de la empresa
}