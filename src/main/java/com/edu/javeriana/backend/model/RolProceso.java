package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * HU-17: Rol de Proceso.
 * Representa un rol funcional de negocio dentro de una empresa
 * (ej: Analista, Supervisor, Auditor).
 * Estos roles se usan para asignar responsabilidades a las actividades
 * de los procesos, de modo que las tareas se asignen a funciones y no a personas.
 *
 * Diferencia con RolPool:
 * - RolPool = permisos administrativos dentro de un Pool (crear/editar/eliminar procesos).
 * - RolProceso = roles funcionales de negocio para actividades (quién ejecuta la tarea).
 */
@Entity
@Table(name = "rol_proceso", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"empresa_id", "nombre"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol de proceso es obligatorio")
    @Column(nullable = false)
    private String nombre; // ej: "Analista", "Supervisor", "Auditor"

    @Column(length = 500)
    private String descripcion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
