package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.model.InstanciaProceso;
import com.edu.javeriana.backend.model.EstadoInstancia;
import com.edu.javeriana.backend.repository.InstanciaProcesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstanciaProcesoService implements IInstanciaProcesoService {

    private final InstanciaProcesoRepository instanciaProcesoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InstanciaProceso> listarActivasPorBusinessKeyYProceso(String businessKey, Long procesoId) {
        return instanciaProcesoRepository.findByBusinessKeyAndProcesoIdAndEstado(businessKey, procesoId,
                EstadoInstancia.ACTIVA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanciaProceso> listarActivasPorProceso(Long procesoId) {
        return instanciaProcesoRepository.findByProcesoIdAndEstado(procesoId, EstadoInstancia.ACTIVA);
    }

    @Override
    @Transactional
    public InstanciaProceso guardarInstancia(InstanciaProceso instancia) {
        return instanciaProcesoRepository.save(instancia);
    }
}
