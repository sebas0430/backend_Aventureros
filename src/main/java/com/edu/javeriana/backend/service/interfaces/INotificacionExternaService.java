package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.model.ConectorExterno;
import com.edu.javeriana.backend.model.NotificacionExterna;

import java.util.List;

public interface INotificacionExternaService {

    ConectorExterno crearConector(ConectorExternoRegistroDTO dto);

    ConectorExterno editarConector(Long id, ConectorExternoRegistroDTO dto);

    void eliminarConector(Long id, Long usuarioId);

    List<ConectorExterno> listarConectoresPorEmpresa(Long empresaId);

    boolean validarCredencialExterna(String token, Long empresaId);

    NotificacionExterna enviarMensajeExterno(EnvioExternoDTO dto);

    List<NotificacionExterna> listarLogsPorProceso(Long procesoId);

    List<NotificacionExterna> listarLogsPorConector(Long conectorId);
}
