package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {
    
    List<Lane> findByPoolId(Long poolId);
    
}
