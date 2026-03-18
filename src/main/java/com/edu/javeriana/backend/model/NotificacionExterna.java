package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de cada intento de envío de un mensaje a un sistema externo.
 * Sirve como log de auditoría y trazabilidad de entregas.
 */
@Entity
@Table(name = "notificacion_externa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conector_id", nullable = false)
    private ConectorExterno conector;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    /** Payload JSON enviado al destino externo */
    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnvioExterno estado;

    @Column(name = "intentos_realizados", nullable = false)
    @Builder.Default
    private int intentosRealizados = 0;

    @Column(name = "detalle_error", length = 2000)
    private String detalleError;

    @Column(name = "respuesta_externa", columnDefinition = "TEXT")
    private String respuestaExterna;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
