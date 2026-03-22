package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El tipo de actividad es obligatorio")
    @Column(name = "tipo_actividad", nullable = false)
    private String tipoActividad;

    @NotBlank(message = "La descripción de la actividad es obligatoria")
    @Column(nullable = false, length = 1000)
    private String descripcion;

    // Rol responsable basado en RolGlobal (HU-06)
    @NotNull(message = "El rol responsable es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_responsable", nullable = false)
    private RolGlobal rolResponsable;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;

    // Orden dentro del proceso (para ajustar el flujo al eliminar)
    @Column(name = "orden")
    private Integer orden;

    // Relación con Proceso (HU-08: la actividad queda vinculada al proceso)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    // HU-17/19: Relación opcional con RolProceso (rol funcional de negocio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_proceso_id", nullable = true)
    private RolProceso rolProceso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}