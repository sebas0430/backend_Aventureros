package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Concepto de Lane (Swimlane) vs Pool:
 * Un Pool representa los límites externos de un proceso a nivel organizacional (por ejemplo, una empresa).
 * Un Lane es una subdivisión de un Pool que se usa para organizar y categorizar responsabilidades. 
 * Generalmente representa un rol, un departamento o un sistema específico dentro de ese Pool 
 * que ejecuta ciertas tareas (actividades).
 */
@Entity
@Table(name = "lane")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del lane es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // TODO: En un futuro aquí mapearemos la relación @OneToMany hacia las Actividades (List<Actividad>) 
    // que se ejecutan específicamente dentro de este rol/departamento/lane.
}
