package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.EstadoInstancia;
import com.edu.javeriana.backend.model.InstanciaProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
    // removed Optional

@Repository
public interface InstanciaProcesoRepository extends JpaRepository<InstanciaProceso, Long> {

    List<InstanciaProceso> findByProcesoId(Long procesoId);

    List<InstanciaProceso> findByBusinessKeyAndProcesoIdAndEstado(String businessKey, Long procesoId, EstadoInstancia estado);

    List<InstanciaProceso> findByProcesoIdAndEstado(Long procesoId, EstadoInstancia estado);
}
