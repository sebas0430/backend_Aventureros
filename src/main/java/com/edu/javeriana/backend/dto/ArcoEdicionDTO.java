package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArcoEdicionDTO {

    @NotNull(message = "El ID del nodo origen es obligatorio")
    private Long origenId;

    @NotBlank(message = "El tipo del nodo origen es obligatorio")
    private String origenTipo; // "ACTIVIDAD" o "GATEWAY"

    @NotNull(message = "El ID del nodo destino es obligatorio")
    private Long destinoId;

    @NotBlank(message = "El tipo del nodo destino es obligatorio")
    private String destinoTipo; // "ACTIVIDAD" o "GATEWAY"

    @NotNull(message = "El ID del usuario que edita el arco es obligatorio")
    private Long usuarioId;

    // Opcional
    private String etiqueta;
}
