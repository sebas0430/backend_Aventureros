package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;

import java.util.List;

public interface IMessageCatchService {

    List<MensajeCatchDTO> recibirMensaje(MensajeCatchDTO dto);
 
    List<MensajeCatchDTO> listarRecepcionesPorProceso(Long procesoId);
 
    List<MensajeCatchDTO> listarRecepcionesPorCatch(Long eventoCatchId);
}
