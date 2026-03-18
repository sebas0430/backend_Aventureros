package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.ProcesoCompartido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcesoCompartidoRepository extends JpaRepository<ProcesoCompartido, Long> {

    List<ProcesoCompartido> findByProcesoId(Long procesoId);

    List<ProcesoCompartido> findByPoolDestinoId(Long poolDestinoId);

    Optional<ProcesoCompartido> findByProcesoIdAndPoolDestinoId(Long procesoId, Long poolDestinoId);
    
    void deleteByProcesoIdAndPoolDestinoId(Long procesoId, Long poolDestinoId);
}
