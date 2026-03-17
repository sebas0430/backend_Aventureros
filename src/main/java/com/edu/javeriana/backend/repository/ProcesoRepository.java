package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {

    // Todos los procesos de una empresa (Aislamiento Crítico)
    List<Proceso> findByEmpresaId(Long empresaId);

    // Todos los procesos creados por un usuario (Trazabilidad)
    List<Proceso> findByAutorId(Long autorId);

    // Procesos de una empresa filtrados por autor
    List<Proceso> findByEmpresaIdAndAutorId(Long empresaId, Long autorId);

    // Procesos filtrados por estado
    List<Proceso> findByEstado(com.edu.javeriana.backend.model.EstadoProceso estado);

    // Procesos de una empresa filtrados por estado+
    List<Proceso> findByEmpresaIdAndEstado(Long empresaId, com.edu.javeriana.backend.model.EstadoProceso estado);
}
