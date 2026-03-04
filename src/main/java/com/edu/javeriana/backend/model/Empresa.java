package com.edu.javeriana.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    @Column(unique = true, nullable = false)
    private String nit;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El correo de contacto es obligatorio")
    @Column(name = "correo_contacto", nullable = false)
    private String correoContacto;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Usuario> usuarios;
}
