package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.Usuario;

public interface IHistorialProcesoService {
    void registrarAccion(Proceso proceso, Usuario usuario, String accion, String detalle);
}
