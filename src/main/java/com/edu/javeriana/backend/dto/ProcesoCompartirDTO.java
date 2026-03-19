package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.PermisoCompartido;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcesoCompartirDTO {

    @NotNull(message = "El ID del pool de destino es obligatorio")
    private Long poolDestinoId;

    @NotNull(message = "El nivel de permiso es obligatorio (ej. LECTURA o LECTURA_ESCRITURA)")
    private PermisoCompartido permiso;

    @NotNull(message = "El ID del usuario que autoriza la compartición es obligatorio")
    private Long usuarioId; // Se validara que sea ADMIN_EMPRESA
}
