package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeEjecucionDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;

import java.util.List;

public interface IEventoMensajeService {

    EventoMensajeRegistroDTO crearEvento(EventoMensajeRegistroDTO dto);

    List<EventoMensajeRegistroDTO> listarPorProceso(Long procesoId);

    void eliminarEvento(Long eventoId, Long usuarioId);

    List<MensajeEjecucionDTO> lanzarMensaje(MensajeLanzarDTO dto);

    List<MensajeEjecucionDTO> listarHistorialPorEventoOrigen(Long eventoOrigenId);
}