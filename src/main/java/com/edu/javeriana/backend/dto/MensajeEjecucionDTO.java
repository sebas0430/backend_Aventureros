package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.EstadoMensaje;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensajeEjecucionDTO {

    private Long id;

    private Long eventoOrigenId;

    private Long eventoDestinoId;

    private String payload;

    private EstadoMensaje estado;

    private String detalleError;

    private LocalDateTime createdAt;
}
