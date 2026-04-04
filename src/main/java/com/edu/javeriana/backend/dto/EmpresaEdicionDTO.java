package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpresaEdicionDTO {

    // Dependiendo de lo que regreses, si también es tu Response, podrías querer el ID
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;
}
