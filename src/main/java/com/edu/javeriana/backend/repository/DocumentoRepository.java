package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByProcesoId(Long procesoId);
}
