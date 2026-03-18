package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.ComportamientoFallback;
import com.edu.javeriana.backend.model.TipoEventoMensaje;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventoMensajeRegistroDTO {

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    private String nombreMensaje;

    @NotNull(message = "El tipo de evento (THROW/CATCH) es obligatorio")
    private TipoEventoMensaje tipo;

    private String payloadSchema;

    private ComportamientoFallback fallback;

    @NotNull(message = "El ID del proceso es obligatorio")
    private Long procesoId;

    @NotNull(message = "El ID del usuario es obligatorio para verificar permisos")
    private Long usuarioId;
}
