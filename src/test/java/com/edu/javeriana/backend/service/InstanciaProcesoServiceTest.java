package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.model.EstadoInstancia;
import com.edu.javeriana.backend.model.InstanciaProceso;
import com.edu.javeriana.backend.repository.InstanciaProcesoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanciaProcesoServiceTest {

    @Mock
    private InstanciaProcesoRepository instanciaProcesoRepository;

    @InjectMocks
    private InstanciaProcesoService instanciaProcesoService;

    @Test
    void listarActivasPorBusinessKeyYProceso() {
        when(instanciaProcesoRepository.findByBusinessKeyAndProcesoIdAndEstado("BK1", 1L, EstadoInstancia.ACTIVA))
                .thenReturn(Collections.emptyList());
        
        List<InstanciaProceso> list = instanciaProcesoService.listarActivasPorBusinessKeyYProceso("BK1", 1L);
        assertTrue(list.isEmpty());
    }

    @Test
    void listarActivasPorProceso() {
        when(instanciaProcesoRepository.findByProcesoIdAndEstado(1L, EstadoInstancia.ACTIVA))
                .thenReturn(Collections.emptyList());
        
        List<InstanciaProceso> list = instanciaProcesoService.listarActivasPorProceso(1L);
        assertTrue(list.isEmpty());
    }

    @Test
    void guardarInstancia() {
        InstanciaProceso in = new InstanciaProceso();
        when(instanciaProcesoRepository.save(any())).thenReturn(in);
        
        InstanciaProceso res = instanciaProcesoService.guardarInstancia(in);
        assertNotNull(res);
        verify(instanciaProcesoRepository).save(in);
    }
}
