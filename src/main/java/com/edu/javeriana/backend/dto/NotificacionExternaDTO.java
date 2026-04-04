package com.edu.javeriana.backend.dto;

import com.edu.javeriana.backend.model.EstadoEnvioExterno;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacionExternaDTO {

    private Long id;

    private Long conectorId;

    private Long procesoId;

    private String payload;

    private EstadoEnvioExterno estado;

    private int intentosRealizados;

    private String detalleError;

    private String respuestaExterna;

    private LocalDateTime createdAt;
}