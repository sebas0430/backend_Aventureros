package com.edu.javeriana.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "arco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Arco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    // ── Nodo de ORIGEN ──
    @NotNull(message = "El ID del nodo origen es obligatorio")
    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @NotNull(message = "El tipo del nodo origen es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "origen_tipo", nullable = false)
    private TipoNodo origenTipo;

    // ── Nodo de DESTINO ──
    @NotNull(message = "El ID del nodo destino es obligatorio")
    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @NotNull(message = "El tipo del nodo destino es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "destino_tipo", nullable = false)
    private TipoNodo destinoTipo;

    // Etiqueta opcional para el arco (ej: "Sí", "No" en un gateway)
    @Column(length = 255)
    private String etiqueta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
