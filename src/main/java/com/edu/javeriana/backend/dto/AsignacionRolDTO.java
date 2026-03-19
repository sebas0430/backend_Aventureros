package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionRolDTO {

    @NotNull(message = "El ID del usuario destinatario es obligatorio")
    private Long usuarioDestinoId;

    @NotNull(message = "El ID del rol configurado del pool es obligatorio")
    private Long rolPoolId;

    @NotNull(message = "El ID del pool es obligatorio")
    private Long poolId;

    @NotNull(message = "El usuario que asigna (ADMINISTRADOR) es obligatorio para permisos")
    private Long usuarioId; // Quién realiza la acción
}
