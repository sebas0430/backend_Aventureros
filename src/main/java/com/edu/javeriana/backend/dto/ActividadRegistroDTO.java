package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.RolGlobal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActividadRegistroDTO {

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    private String tipoActividad;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    private String descripcion;

    // Rol responsable (HU-08 referencia HU-06 gestión de roles)
    @NotNull(message = "El rol responsable es obligatorio")
    private RolGlobal rolResponsable;

    // Orden opcional dentro del proceso
    private Integer orden;

    @NotNull(message = "El ID del proceso es obligatorio")
    private Long procesoId;

    // Usuario que crea la actividad (para validar permisos)
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
}