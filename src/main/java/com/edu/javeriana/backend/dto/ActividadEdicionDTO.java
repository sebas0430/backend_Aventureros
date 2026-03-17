package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.RolGlobal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActividadEdicionDTO {

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    private String tipoActividad;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    private String descripcion;

    @NotNull(message = "El rol responsable es obligatorio")
    private RolGlobal rolResponsable;

    private Integer orden;

    // Usuario que edita (para validar permisos y guardar en historial)
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
}