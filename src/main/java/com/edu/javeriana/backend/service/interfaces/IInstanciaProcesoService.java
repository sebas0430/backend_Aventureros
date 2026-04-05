package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.model.InstanciaProceso;

import java.util.List;

public interface IInstanciaProcesoService {
    List<InstanciaProceso> listarActivasPorBusinessKeyYProceso(String businessKey, Long procesoId);

    List<InstanciaProceso> listarActivasPorProceso(Long procesoId);

    InstanciaProceso guardarInstancia(InstanciaProceso instancia);
}
