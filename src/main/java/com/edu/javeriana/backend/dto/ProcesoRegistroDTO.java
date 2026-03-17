package com.edu.javeriana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProcesoRegistroDTO {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción del proceso es obligatoria")
    private String descripcion;

    @NotBlank(message = "La categoría del proceso es obligatoria")
    private String categoria;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;

    @NotNull(message = "El ID del autor es obligatorio")
    private Long autorId;

    private Long poolId; // Opcional: Si no se manda, irá al pool por defecto de la empresa

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public Long getAutorId() { return autorId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }

    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
}