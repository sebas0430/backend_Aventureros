package com.edu.javeriana.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HU-20: DTO de respuesta para consultar un rol de proceso con detalle
 * de en qué procesos y actividades está siendo utilizado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolProcesoDetalleDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long empresaId;
    private List<ProcesoUsoDTO> usoEnProcesos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcesoUsoDTO {
        private Long procesoId;
        private String procesoNombre;
        private List<ActividadUsoDTO> actividades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadUsoDTO {
        private Long actividadId;
        private String actividadNombre;
        private String tipoActividad;
    }
}
