package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.dto.NotificacionExternaDTO;

import java.util.List;

public interface INotificacionExternaService {

    ConectorExternoRegistroDTO crearConector(ConectorExternoRegistroDTO dto);

    ConectorExternoRegistroDTO editarConector(Long id, ConectorExternoRegistroDTO dto);

    void eliminarConector(Long id, Long usuarioId);

    List<ConectorExternoRegistroDTO> listarConectoresPorEmpresa(Long empresaId);

    NotificacionExternaDTO enviarMensajeExterno(EnvioExternoDTO dto);

    List<NotificacionExternaDTO> listarLogsPorProceso(Long procesoId);

    List<NotificacionExternaDTO> listarLogsPorConector(Long conectorId);
}