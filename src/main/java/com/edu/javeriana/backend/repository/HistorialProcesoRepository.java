package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.HistorialProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {
    List<HistorialProceso> findByProcesoIdOrderByFechaDesc(Long procesoId);
}