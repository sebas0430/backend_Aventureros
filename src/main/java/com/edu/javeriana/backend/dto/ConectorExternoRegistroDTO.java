package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.TipoConectorExterno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConectorExternoRegistroDTO {

    @NotBlank(message = "El nombre del conector es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de conector (EMAIL, WEBHOOK, QUEUE) es obligatorio")
    private TipoConectorExterno tipo;

    @NotBlank(message = "El destino (URL/host) es obligatorio")
    private String destino;

    private Integer puerto;

    private String credencialRef;

    private String usuarioAuth;

    private String headersJson;

    private int maxReintentos;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del usuario solicitante es obligatorio")
    private Long usuarioId;
}
