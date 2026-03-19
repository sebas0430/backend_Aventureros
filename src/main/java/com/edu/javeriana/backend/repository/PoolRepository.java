package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Long> {
    
    List<Pool> findByEmpresaId(Long empresaId);
    
    Optional<Pool> findFirstByEmpresaIdOrderByIdAsc(Long empresaId);
}
