package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    // Todos los procesos de una empresa (Aislamiento Crítico) - SOLO ACTIVOS
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId AND p.estado <> com.edu.javeriana.backend.model.EstadoProceso.INACTIVO")
    List<Proceso> findByEmpresaIdActive(@org.springframework.data.repository.query.Param("empresaId") Long empresaId);

    // Todos los procesos de una empresa (Aislamiento Crítico) - INCLUYENDO INACTIVOS (para trazabilidad/admin)
    List<Proceso> findByEmpresaId(Long empresaId);

    // Todos los procesos creados por un usuario (Trazabilidad)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Proceso p WHERE p.autor.id = :autorId AND p.estado <> com.edu.javeriana.backend.model.EstadoProceso.INACTIVO")
    List<Proceso> findByAutorIdActive(@org.springframework.data.repository.query.Param("autorId") Long autorId);

    // Todos los procesos creados por un usuario (Trazabilidad)
    List<Proceso> findByAutorId(Long autorId);

    // Procesos de una empresa filtrados por autor
    List<Proceso> findByEmpresaIdAndAutorId(Long empresaId, Long autorId);

    // Procesos filtrados por estado
    List<Proceso> findByEstado(com.edu.javeriana.backend.model.EstadoProceso estado);

    // Procesos de una empresa filtrados por estado
    List<Proceso> findByEmpresaIdAndEstado(Long empresaId, com.edu.javeriana.backend.model.EstadoProceso estado);

    // Consulta con filtros opcionales de estado y categoria
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Proceso p WHERE p.empresa.id = :empresaId " +
            "AND (:estado IS NULL OR p.estado = :estado) " +
            "AND (:categoria IS NULL OR p.categoria LIKE %:categoria%) " +
            "AND (:estado IS NOT NULL OR p.estado <> com.edu.javeriana.backend.model.EstadoProceso.INACTIVO)")
    List<Proceso> buscarConFiltros(@org.springframework.data.repository.query.Param("empresaId") Long empresaId,
            @org.springframework.data.repository.query.Param("estado") com.edu.javeriana.backend.model.EstadoProceso estado,
            @org.springframework.data.repository.query.Param("categoria") String categoria);
}