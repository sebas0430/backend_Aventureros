package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ConectorExternoRegistroDTO;
import com.edu.javeriana.backend.dto.EnvioExternoDTO;
import com.edu.javeriana.backend.dto.NotificacionExternaDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.ConectorExternoRepository;
import com.edu.javeriana.backend.repository.NotificacionExternaRepository;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
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
class NotificacionExternaServiceTest {

    @Mock
    private ConectorExternoRepository conectorExternoRepository;
    @Mock
    private NotificacionExternaRepository notificacionExternaRepository;
    @Mock
    private IEmpresaService empresaService;
    @Mock
    private IProcesoService procesoService;
    @Mock
    private IUsuarioService usuarioService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private NotificacionExternaService notificacionExternaService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private ConectorExterno conector;
    private Proceso proceso;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        conector = ConectorExterno.builder()
                .id(1L)
                .nombre("Webhook 1")
                .tipo(TipoConectorExterno.WEBHOOK)
                .empresa(empresa)
                .activo(true)
                .maxReintentos(3)
                .build();
    }

    @Test
    void crearConector_Exitoso() {
        ConectorExternoRegistroDTO dto = new ConectorExternoRegistroDTO();
        dto.setEmpresaId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Nuevo Conector");
        dto.setTipo(TipoConectorExterno.EMAIL);

        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(conectorExternoRepository.save(any(ConectorExterno.class))).thenReturn(conector);
        when(modelMapper.map(any(), eq(ConectorExternoRegistroDTO.class))).thenReturn(dto);

        ConectorExternoRegistroDTO res = notificacionExternaService.crearConector(dto);

        assertNotNull(res);
        verify(conectorExternoRepository).save(any());
    }

    @Test
    void editarConector_Exitoso() {
        ConectorExternoRegistroDTO dto = new ConectorExternoRegistroDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Nombre Editado");
        dto.setTipo(TipoConectorExterno.WEBHOOK);

        when(conectorExternoRepository.findById(1L)).thenReturn(Optional.of(conector));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(conectorExternoRepository.save(any())).thenReturn(conector);
        when(modelMapper.map(any(), eq(ConectorExternoRegistroDTO.class))).thenReturn(dto);

        ConectorExternoRegistroDTO res = notificacionExternaService.editarConector(1L, dto);
        assertNotNull(res);
        assertEquals("Nombre Editado", conector.getNombre());
    }

    @Test
    void eliminarConector_Exitoso() {
        when(conectorExternoRepository.findById(1L)).thenReturn(Optional.of(conector));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        notificacionExternaService.eliminarConector(1L, 1L);
        verify(conectorExternoRepository).delete(conector);
    }

    @Test
    void enviarMensajeExterno_Exitoso() {
        EnvioExternoDTO dto = new EnvioExternoDTO();
        dto.setConectorId(1L);
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setPayload("{}");

        when(conectorExternoRepository.findById(1L)).thenReturn(Optional.of(conector));
        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        
        NotificacionExterna notif = new NotificacionExterna();
        notif.setConector(conector);
        notif.setProceso(proceso);
        when(notificacionExternaRepository.save(any())).thenReturn(notif);
        when(modelMapper.map(any(), eq(NotificacionExternaDTO.class))).thenReturn(new NotificacionExternaDTO());

        NotificacionExternaDTO res = notificacionExternaService.enviarMensajeExterno(dto);
        assertNotNull(res);
        assertEquals(EstadoEnvioExterno.ENVIADO, notif.getEstado());
    }

    @Test
    void enviarMensajeExterno_ConectorInactivo() {
        conector.setActivo(false);
        EnvioExternoDTO dto = new EnvioExternoDTO();
        dto.setConectorId(1L);
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);

        when(conectorExternoRepository.findById(1L)).thenReturn(Optional.of(conector));
        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        assertThrows(BusinessRuleException.class, () -> notificacionExternaService.enviarMensajeExterno(dto));
    }

    @Test
    void listarLogsPorProceso() {
        when(notificacionExternaRepository.findByProcesoId(1L)).thenReturn(Collections.emptyList());
        List<NotificacionExternaDTO> list = notificacionExternaService.listarLogsPorProceso(1L);
        assertTrue(list.isEmpty());
    }
}
