package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IHistorialProcesoService;
import com.edu.javeriana.backend.model.HistorialProceso;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.HistorialProcesoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class HistorialProcesoService implements IHistorialProcesoService {

    private final HistorialProcesoRepository historialProcesoRepository;

    public HistorialProcesoService(HistorialProcesoRepository historialProcesoRepository) {
        this.historialProcesoRepository = historialProcesoRepository;
    }

    @Override
    @Transactional
    public void registrarAccion(Proceso proceso, Usuario usuario, String accion, String detalle) {
        // Creamos una entrada en el libro de bitácora del proceso.
        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso)
                .usuario(usuario)
                .accion(accion)
                .detalle(detalle)
                .build();
        
        // Guardamos el movimiento para que luego se pueda auditar quién hizo qué.
        historialProcesoRepository.save(historial);
        log.info("Historial registrado para proceso {}: {}", proceso.getId(), accion);
    }
}
