package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolPoolRegistroDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El id del pool es obligatorio")
    private Long poolId;

    private boolean permisoCrearProceso;
    private boolean permisoEditarProceso;
    private boolean permisoEliminarProceso;
    private boolean permisoPublicarProceso;
    private boolean permisoGestionarRoles;

    @NotNull(message = "El usuario solicitante es obligatorio para verificar permisos")
    private Long usuarioId; 
}
