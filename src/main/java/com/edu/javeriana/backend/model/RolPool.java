package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rol_pool")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Column(nullable = false)
    private String nombre; // e.g., "Editor", "Administrador Delegado", "Lector"

    @Column(length = 500)
    private String descripcion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    // Permisos Dinámicos (Matriz de Configuración)
    @Column(nullable = false)
    @Builder.Default
    private boolean permisoCrearProceso = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean permisoEditarProceso = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean permisoEliminarProceso = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean permisoPublicarProceso = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean permisoGestionarRoles = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
