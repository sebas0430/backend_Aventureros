package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.Arco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArcoRepository extends JpaRepository<Arco, Long> {

    // Todos los arcos de un proceso
    List<Arco> findByProcesoId(Long procesoId);

    // Arcos que salen de un nodo específico
    List<Arco> findByProcesoIdAndOrigenIdAndOrigenTipo(
            Long procesoId, Long origenId, com.edu.javeriana.backend.model.TipoNodo origenTipo);

    // Arcos que llegan a un nodo específico
    List<Arco> findByProcesoIdAndDestinoIdAndDestinoTipo(
            Long procesoId, Long destinoId, com.edu.javeriana.backend.model.TipoNodo destinoTipo);

    // Verificar si ya existe un arco entre dos nodos exactos en un proceso
    boolean existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
            Long procesoId, Long origenId, com.edu.javeriana.backend.model.TipoNodo origenTipo,
            Long destinoId, com.edu.javeriana.backend.model.TipoNodo destinoTipo);

    // Eliminar todos los arcos de un proceso
    void deleteByProcesoId(Long procesoId);
}
