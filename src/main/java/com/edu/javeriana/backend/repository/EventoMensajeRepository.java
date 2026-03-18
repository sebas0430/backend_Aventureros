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

    /** Busca CATCHes activos por nombre de mensaje y empresa (procesos PUBLICADOS) */
    @Query("SELECT e FROM EventoMensaje e JOIN e.proceso p WHERE " +
           "e.nombreMensaje = :nombre AND e.tipo = 'CATCH' AND e.activo = true AND " +
           "p.empresa.id = :empresaId AND p.estado = 'PUBLICADO'")
    List<EventoMensaje> findActiveCatchesByNombreAndEmpresa(
            @Param("nombre") String nombre,
            @Param("empresaId") Long empresaId);

    /** Busca CATCHes activos por nombre + correlación + empresa */
    @Query("SELECT e FROM EventoMensaje e JOIN e.proceso p WHERE " +
           "e.nombreMensaje = :nombre AND e.tipo = 'CATCH' AND e.activo = true AND " +
           "e.correlationKey = :correlationKey AND " +
           "p.empresa.id = :empresaId AND p.estado = 'PUBLICADO'")
    List<EventoMensaje> findActiveCatchesByNombreAndCorrelationAndEmpresa(
            @Param("nombre") String nombre,
            @Param("correlationKey") String correlationKey,
            @Param("empresaId") Long empresaId);
}
