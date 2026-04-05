package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.MensajeCatchDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageCatchServiceTest {

    @Mock
    private EventoMensajeRepository eventoMensajeRepository;
    @Mock
    private RecepcionMensajeRepository recepcionMensajeRepository;
    @Mock
    private ConectorExternoRepository conectorExternoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private InstanciaProcesoRepository instanciaProcesoRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MessageCatchService messageCatchService;

    private Empresa empresa;
    private EventoMensaje catchEvento;
    private MensajeCatchDTO dto;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        catchEvento = EventoMensaje.builder()
                .id(1L)
                .nombreMensaje("M1")
                .tipo(TipoEventoMensaje.CATCH)
                .proceso(proceso)
                .build();

        dto = new MensajeCatchDTO();
        dto.setEmpresaId(1L);
        dto.setNombreMensaje("M1");
        dto.setFuente("INTERNO");
        dto.setPayload("{}");
    }

    @Test
    void recibirMensaje_Exitoso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(eventoMensajeRepository.findActiveCatchesByNombreAndEmpresa(any(), any())).thenReturn(List.of(catchEvento));
        
        InstanciaProceso instancia = new InstanciaProceso();
        instancia.setId(1L);
        when(instanciaProcesoRepository.findByProcesoIdAndEstado(any(), any())).thenReturn(List.of(instancia));
        
        when(recepcionMensajeRepository.save(any())).thenReturn(new RecepcionMensaje());
        when(modelMapper.map(any(), eq(MensajeCatchDTO.class))).thenReturn(dto);

        List<MensajeCatchDTO> res = messageCatchService.recibirMensaje(dto);

        assertNotNull(res);
        assertFalse(res.isEmpty());
        verify(recepcionMensajeRepository).save(any());
    }

    @Test
    void recibirMensaje_SinCatches() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(eventoMensajeRepository.findActiveCatchesByNombreAndEmpresa(any(), any())).thenReturn(Collections.emptyList());

        assertThrows(BusinessRuleException.class, () -> messageCatchService.recibirMensaje(dto));
    }

    @Test
    void recibirMensaje_ExternoTokenInvalido() {
        dto.setFuente("EXTERNO");
        dto.setTokenSeguridad("invalid");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(conectorExternoRepository.findByEmpresaIdAndActivo(1L, true)).thenReturn(Collections.emptyList());

        assertThrows(BusinessRuleException.class, () -> messageCatchService.recibirMensaje(dto));
    }

    @Test
    void recibirMensaje_CrearInstanciaSiFalla() {
        catchEvento.setCrearInstanciaSiFalla(true);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(eventoMensajeRepository.findActiveCatchesByNombreAndEmpresa(any(), any())).thenReturn(List.of(catchEvento));
        when(instanciaProcesoRepository.findByProcesoIdAndEstado(any(), any())).thenReturn(Collections.emptyList());
        
        InstanciaProceso nueva = new InstanciaProceso();
        nueva.setId(2L);
        when(instanciaProcesoRepository.save(any(InstanciaProceso.class))).thenReturn(nueva);
        when(recepcionMensajeRepository.save(any())).thenReturn(new RecepcionMensaje());
        when(modelMapper.map(any(), eq(MensajeCatchDTO.class))).thenReturn(dto);

        List<MensajeCatchDTO> res = messageCatchService.recibirMensaje(dto);
        assertNotNull(res);
        verify(instanciaProcesoRepository, times(2)).save(any());
    }

    @Test
    void listarRecepcionesPorProceso() {
        when(recepcionMensajeRepository.findByEventoCatchProcesoId(1L)).thenReturn(Collections.emptyList());
        List<MensajeCatchDTO> list = messageCatchService.listarRecepcionesPorProceso(1L);
        assertTrue(list.isEmpty());
    }
}
