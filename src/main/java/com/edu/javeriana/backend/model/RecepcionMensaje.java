package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de recepción de un mensaje por un CATCH.
 * Almacena el payload recibido, las variables mapeadas al proceso receptor,
 * el resultado de la validación de credenciales, y la acción tomada.
 */
@Entity
@Table(name = "recepcion_mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_catch_id", nullable = false)
    @JsonIgnore
    private EventoMensaje eventoCatch;

    /** Fuente del mensaje: INTERNO (otro proceso) o EXTERNO (sistema externo) */
    @Column(nullable = false, length = 50)
    private String fuenteMensaje;

    /** Identificador del emisor (ID del proceso o nombre del sistema externo) */
    @Column(length = 500)
    private String identificadorEmisor;

    /** Payload JSON recibido */
    @Column(columnDefinition = "TEXT")
    private String payloadRecibido;

    /** Variables del proceso actualizadas tras el mapeo del payload */
    @Column(name = "variables_mapeadas", columnDefinition = "TEXT")
    private String variablesMapeadas;

    /** Correlation key usada para emparejar */
    @Column(name = "correlation_key_usada", length = 500)
    private String correlationKeyUsada;

    /** Resultado de la validación de credenciales del emisor */
    @Column(nullable = false)
    @Builder.Default
    private boolean credencialValidada = false;

    /** Acción tomada: ACTIVAR_INSTANCIA, CONTINUAR_INSTANCIA, RECHAZADO */
    @Column(nullable = false, length = 50)
    private String accionTomada;

    @Column(length = 1000)
    private String detalle;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
