package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MensajeCatchDTO {

    @NotBlank(message = "El nombre del mensaje es obligatorio")
    private String nombreMensaje;

    /** Clave de correlación para emparejar con un CATCH específico */
    private String correlationKey;

    /** Payload JSON con datos del emisor para mapear a variables del proceso */
    private String payload;

    /** Fuente del mensaje: INTERNO o EXTERNO */
    @NotBlank(message = "La fuente (INTERNO/EXTERNO) es obligatoria")
    private String fuente;

    /** Identificador del emisor (proceso ID o nombre del sistema) */
    private String identificadorEmisor;

    /** ID de la empresa destino (para enrutar el mensaje) */
    @NotNull(message = "El ID de la empresa destino es obligatorio")
    private Long empresaId;

    /**
     * Token/firma de seguridad del emisor externo.
     * Se valida contra la credencialRef del conector externo configurado
     * para evitar inyección de datos por fuentes no autorizadas.
     */
    private String tokenSeguridad;
}
