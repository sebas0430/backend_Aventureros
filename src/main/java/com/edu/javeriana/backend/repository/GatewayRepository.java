package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatewayRepository extends JpaRepository<Gateway, Long> {
    List<Gateway> findByProcesoId(Long procesoId);
}
