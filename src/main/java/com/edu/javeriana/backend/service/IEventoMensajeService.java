package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.model.EventoMensaje;
import com.edu.javeriana.backend.model.MensajeEjecucion;

import java.util.List;

public interface IEventoMensajeService {

    EventoMensaje crearEvento(EventoMensajeRegistroDTO dto);

    List<EventoMensaje> listarPorProceso(Long procesoId);

    void eliminarEvento(Long eventoId, Long usuarioId);

    List<MensajeEjecucion> lanzarMensaje(MensajeLanzarDTO dto);

    List<MensajeEjecucion> listarHistorialPorEventoOrigen(Long eventoOrigenId);
}
