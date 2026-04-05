package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.model.HistorialProceso;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.HistorialProcesoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HistorialProcesoServiceTest {

    @Mock
    private HistorialProcesoRepository historialProcesoRepository;

    @InjectMocks
    private HistorialProcesoService historialProcesoService;

    @Test
    void registrarAccion_Exitoso() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        historialProcesoService.registrarAccion(proceso, usuario, "TEST_ACCION", "Detalle");

        verify(historialProcesoRepository).save(any(HistorialProceso.class));
    }
}
