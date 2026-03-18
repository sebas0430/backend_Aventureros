package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del mensaje a enviar/recibir es obligatorio")
    @Column(nullable = false)
    private String nombreMensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoMensaje tipo;

    @Column(columnDefinition = "TEXT")
    private String payloadSchema;

    @Enumerated(EnumType.STRING)
    private ComportamientoFallback fallback;

    /** Clave de correlación para emparejar mensajes entrantes con instancias específicas */
    @Column(name = "correlation_key", length = 500)
    private String correlationKey;

    /** Indica si este CATCH está activo y esperando mensajes */
    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    @JsonIgnore
    private Proceso proceso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
