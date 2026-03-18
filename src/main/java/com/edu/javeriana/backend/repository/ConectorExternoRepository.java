package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.ConectorExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConectorExternoRepository extends JpaRepository<ConectorExterno, Long> {
    List<ConectorExterno> findByEmpresaId(Long empresaId);
    List<ConectorExterno> findByEmpresaIdAndActivo(Long empresaId, boolean activo);
}
