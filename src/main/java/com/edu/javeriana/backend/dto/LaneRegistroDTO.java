package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LaneRegistroDTO {

    @NotBlank(message = "El nombre del lane es obligatorio (ej. Gerencia de Sistemas)")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El ID del pool al que pertenece el lane es obligatorio")
    private Long poolId;

    @NotNull(message = "El ID del usuario (quien crea el lane) es obligatorio")
    private Long usuarioId; // Se validara a traves del servicio si el creador es de la misma empresa
}
