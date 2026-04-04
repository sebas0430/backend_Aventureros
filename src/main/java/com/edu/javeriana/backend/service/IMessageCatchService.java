package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.model.RecepcionMensaje;

import java.util.List;

public interface IMessageCatchService {

    List<MensajeCatchDTO> recibirMensaje(MensajeCatchDTO dto);
 
    List<MensajeCatchDTO> listarRecepcionesPorProceso(Long procesoId);
 
    List<MensajeCatchDTO> listarRecepcionesPorCatch(Long eventoCatchId);
}
