package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proceso es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La descripción del proceso es obligatoria")
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @NotBlank(message = "La categoría del proceso es obligatoria")
    @Column(nullable = false)
    private String categoria;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProceso estado = EstadoProceso.BORRADOR;

    @Lob
    @Column(name = "definicion_json", columnDefinition = "TEXT")
    private String definicionJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Arco> arcos;

    @JsonIgnore
    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gateway> gateways;

    @JsonIgnore
@OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Actividad> actividades;
}