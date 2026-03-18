package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.RolPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPoolRepository extends JpaRepository<RolPool, Long> {
    List<RolPool> findByPoolId(Long poolId);
    boolean existsByPoolIdAndNombre(Long poolId, String nombre);
}
