package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.model.RecepcionMensaje;

import java.util.List;

public interface IMessageCatchService {

    List<RecepcionMensaje> recibirMensaje(MensajeCatchDTO dto);

    List<RecepcionMensaje> listarRecepcionesPorProceso(Long procesoId);

    List<RecepcionMensaje> listarRecepcionesPorCatch(Long eventoCatchId);
}
