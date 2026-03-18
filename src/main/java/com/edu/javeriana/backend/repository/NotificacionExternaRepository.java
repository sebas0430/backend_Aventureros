package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.NotificacionExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionExternaRepository extends JpaRepository<NotificacionExterna, Long> {
    List<NotificacionExterna> findByProcesoId(Long procesoId);
    List<NotificacionExterna> findByConectorId(Long conectorId);
}
