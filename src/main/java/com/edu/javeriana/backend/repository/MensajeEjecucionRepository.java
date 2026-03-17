package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.MensajeEjecucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeEjecucionRepository extends JpaRepository<MensajeEjecucion, Long> {

    List<MensajeEjecucion> findByEventoOrigenId(Long eventoOrigenId);
    
    List<MensajeEjecucion> findByEventoDestinoId(Long eventoDestinoId);
}
