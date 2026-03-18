package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.AsignacionRolPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRolPoolRepository extends JpaRepository<AsignacionRolPool, Long> {
    
    List<AsignacionRolPool> findByPoolId(Long poolId);
    
    Optional<AsignacionRolPool> findByUsuarioIdAndPoolId(Long usuarioId, Long poolId);

    List<AsignacionRolPool> findByRolId(Long rolId);
    
    boolean existsByRolId(Long rolId);
}
