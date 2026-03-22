package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // Listar actividades activas de un proceso ordenadas por campo orden
    List<Actividad> findByProcesoIdAndActivaTrueOrderByOrdenAsc(Long procesoId);

    // Listar todas (incluyendo inactivas) — útil para administradores
    List<Actividad> findByProcesoIdOrderByOrdenAsc(Long procesoId);

    // Contar actividades activas en un proceso (para reordenar al eliminar)
    long countByProcesoIdAndActivaTrue(Long procesoId);

    // HU-19: Validar si alguna actividad tiene asignado un rol de proceso
    boolean existsByRolProcesoId(Long rolProcesoId);
}