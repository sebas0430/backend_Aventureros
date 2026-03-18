package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.RecepcionMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecepcionMensajeRepository extends JpaRepository<RecepcionMensaje, Long> {

    List<RecepcionMensaje> findByEventoCatchId(Long eventoCatchId);

    List<RecepcionMensaje> findByEventoCatchProcesoId(Long procesoId);
}
