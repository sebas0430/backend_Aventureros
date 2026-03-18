package com.edu.javeriana.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Configuración de un conector externo a nivel de empresa.
 * Almacena los datos de conexión (URL/host, credenciales seguras, tipo)
 * para que las tareas de integración puedan disparar envíos.
 *
 * Las credenciales/secretos se almacenan cifrados o referenciados
 * desde variables de entorno del servidor (nunca en texto plano en producción).
 */
@Entity
@Table(name = "conector_externo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConectorExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del conector es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotNull(message = "El tipo de conector es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConectorExterno tipo;

    /** URL del webhook, host SMTP o dirección del broker */
    @NotBlank(message = "El destino (URL/host) es obligatorio")
    @Column(nullable = false, length = 1000)
    private String destino;

    /** Puerto (para SMTP o broker). Nullable para webhooks. */
    private Integer puerto;

    /**
     * Referencia al secreto/credencial.
     * En producción esto debería apuntar a un vault o variable de entorno.
     * Ejemplo: "ENV:SMTP_PASSWORD" o "VAULT:webhook-token-xyz"
     */
    @Column(name = "credencial_ref", length = 500)
    private String credencialRef;

    /** Usuario de autenticación (SMTP user, API key name, etc.) */
    @Column(length = 500)
    private String usuarioAuth;

    /** Cabeceras adicionales en formato JSON (para webhooks) */
    @Column(name = "headers_json", columnDefinition = "TEXT")
    private String headersJson;

    /** Número máximo de reintentos en caso de fallo */
    @Column(nullable = false)
    @Builder.Default
    private int maxReintentos = 3;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

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
