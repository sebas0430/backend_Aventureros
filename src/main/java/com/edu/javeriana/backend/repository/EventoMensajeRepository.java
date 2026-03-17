package com.edu.javeriana.backend.repository;

import com.edu.javeriana.backend.model.EventoMensaje;
import com.edu.javeriana.backend.model.TipoEventoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
    // removed Optional

@Repository
public interface EventoMensajeRepository extends JpaRepository<EventoMensaje, Long> {

    List<EventoMensaje> findByProcesoId(Long procesoId);

    @Query("SELECT e FROM EventoMensaje e JOIN e.proceso p WHERE " +
           "e.nombreMensaje = :nombre AND e.tipo = :tipo AND " +
           "p.empresa.id = :empresaId AND p.estado = :estado")
    List<EventoMensaje> findByNombreMensajeAndTipoAndEmpresaIdAndEstado(
            @Param("nombre") String nombre, 
            @Param("tipo") TipoEventoMensaje tipo, 
            @Param("empresaId") Long empresaId,
            @Param("estado") com.edu.javeriana.backend.model.EstadoProceso estado);
}
