package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.RolProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {

    List<RolProceso> findByEmpresaId(Long empresaId);

    boolean existsByEmpresaIdAndNombre(Long empresaId, String nombre);
}
