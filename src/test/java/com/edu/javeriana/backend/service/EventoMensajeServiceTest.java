package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EventoMensajeRegistroDTO;
import com.edu.javeriana.backend.dto.MensajeEjecucionDTO;
import com.edu.javeriana.backend.dto.MensajeLanzarDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;

import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.EventoMensajeRepository;
import com.edu.javeriana.backend.repository.MensajeEjecucionRepository;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
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
class EventoMensajeServiceTest {

    @Mock
    private EventoMensajeRepository eventoMensajeRepository;
    @Mock
    private MensajeEjecucionRepository mensajeEjecucionRepository;
    @Mock
    private IProcesoService procesoService;
    @Mock
    private IUsuarioService usuarioService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EventoMensajeService eventoMensajeService;

    private Proceso proceso;
    private Usuario adminUsuario;
    private EventoMensaje evento;

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        evento = EventoMensaje.builder()
                .id(1L)
                .nombreMensaje("M1")
                .tipo(TipoEventoMensaje.THROW)
                .proceso(proceso)
                .build();
    }

    @Test
    void crearEvento_Exitoso() {
        EventoMensajeRegistroDTO dto = new EventoMensajeRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setNombreMensaje("M1");

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(eventoMensajeRepository.save(any(EventoMensaje.class))).thenReturn(evento);
        when(modelMapper.map(any(), eq(EventoMensajeRegistroDTO.class))).thenReturn(dto);

        EventoMensajeRegistroDTO res = eventoMensajeService.crearEvento(dto);

        assertNotNull(res);
        verify(eventoMensajeRepository).save(any());
    }

    @Test
    void eliminarEvento_Exitoso() {
        when(eventoMensajeRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        eventoMensajeService.eliminarEvento(1L, 1L);
        verify(eventoMensajeRepository).delete(evento);
    }

    @Test
    void lanzarMensaje_Exitoso() {
        MensajeLanzarDTO dto = new MensajeLanzarDTO();
        dto.setEventoOrigenId(1L);
        dto.setUsuarioId(1L);
        dto.setPayload("{}");

        EventoMensaje destino = EventoMensaje.builder().id(2L).proceso(proceso).build();

        when(eventoMensajeRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(eventoMensajeRepository.findByNombreMensajeAndTipoAndEmpresaIdAndEstado(any(), any(), any(), any()))
                .thenReturn(List.of(destino));
        
        MensajeEjecucion ejecucion = new MensajeEjecucion();
        ejecucion.setEventoOrigen(evento);
        ejecucion.setEventoDestino(destino);
        when(mensajeEjecucionRepository.save(any())).thenReturn(ejecucion);
        when(modelMapper.map(any(), eq(MensajeEjecucionDTO.class))).thenReturn(new MensajeEjecucionDTO());

        List<MensajeEjecucionDTO> res = eventoMensajeService.lanzarMensaje(dto);
        assertNotNull(res);
        assertFalse(res.isEmpty());
    }

    @Test
    void lanzarMensaje_SinReceptor_FallbackError() {
        MensajeLanzarDTO dto = new MensajeLanzarDTO();
        dto.setEventoOrigenId(1L);
        dto.setUsuarioId(1L);
        evento.setFallback(ComportamientoFallback.ERROR);

        when(eventoMensajeRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(eventoMensajeRepository.findByNombreMensajeAndTipoAndEmpresaIdAndEstado(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(BusinessRuleException.class, () -> eventoMensajeService.lanzarMensaje(dto));
    }

    @Test
    void listarHistorialPorEventoOrigen() {
        when(mensajeEjecucionRepository.findByEventoOrigenId(1L)).thenReturn(Collections.emptyList());
        List<MensajeEjecucionDTO> list = eventoMensajeService.listarHistorialPorEventoOrigen(1L);
        assertTrue(list.isEmpty());
    }
}
