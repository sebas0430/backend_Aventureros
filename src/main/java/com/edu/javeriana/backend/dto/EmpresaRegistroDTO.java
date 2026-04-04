package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpresaRegistroDTO {
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El correo de contacto es obligatorio")
    private String correoContacto;

    @NotBlank(message = "La contraseña del administrador inicial es obligatoria")
    private String passwordAdmin;
}
