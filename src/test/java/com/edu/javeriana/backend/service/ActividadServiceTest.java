package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Actividad;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.RolGlobal;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ActividadRepository;
import com.edu.javeriana.backend.service.interfaces.IHistorialProcesoService;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private IProcesoService procesoService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IHistorialProcesoService historialProcesoService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ActividadService actividadService;

    private Proceso proceso;
    private Usuario adminUsuario;
    private Usuario normalUsuario;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol(RolGlobal.ADMINISTRADOR_EMPRESA.name());

        normalUsuario = new Usuario();
        normalUsuario.setId(2L);
        normalUsuario.setRol("OBSERVADOR");

        actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Actividad 1");
        actividad.setTipoActividad("SIMPLE");
        actividad.setDescripcion("Desc");
        actividad.setRolResponsable(RolGlobal.ADMINISTRADOR_EMPRESA);
        actividad.setOrden(1);
        actividad.setProceso(proceso);
    }

    @Test
    void crearActividad_Exitoso() {
        ActividadRegistroDTO dto = new ActividadRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);
        
        ActividadRegistroDTO expectedResponse = new ActividadRegistroDTO();
        when(modelMapper.map(any(Actividad.class), eq(ActividadRegistroDTO.class))).thenReturn(expectedResponse);

        ActividadRegistroDTO response = actividadService.crearActividad(dto);

        assertNotNull(response);
        verify(actividadRepository, times(1)).save(any());
        verify(historialProcesoService, times(1)).registrarAccion(eq(proceso), eq(adminUsuario), anyString(), anyString());
    }

    @Test
    void crearActividad_FalloPermisos() {
        ActividadRegistroDTO dto = new ActividadRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(2L);

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(2L)).thenReturn(normalUsuario);

        assertThrows(BusinessRuleException.class, () -> actividadService.crearActividad(dto));
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void editarActividad_Exitoso() {
        ActividadEdicionDTO dto = new ActividadEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Actividad Modificada");
        dto.setTipoActividad("COMPLEJA");
        dto.setDescripcion("Desc modificada");
        dto.setRolResponsable(RolGlobal.EDITOR);
        dto.setOrden(2);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(actividadRepository.save(any(Actividad.class))).thenReturn(actividad);

        ActividadEdicionDTO expectedResponse = new ActividadEdicionDTO();
        when(modelMapper.map(any(Actividad.class), eq(ActividadEdicionDTO.class))).thenReturn(expectedResponse);

        ActividadEdicionDTO response = actividadService.editarActividad(1L, dto);

        assertNotNull(response);
        assertEquals("Actividad Modificada", actividad.getNombre());
        verify(actividadRepository, times(1)).save(actividad);
    }

    @Test
    void editarActividad_NoEncontrado() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.empty());
        ActividadEdicionDTO dto = new ActividadEdicionDTO();
        assertThrows(ResourceNotFoundException.class, () -> actividadService.editarActividad(1L, dto));
    }

    @Test
    void eliminarActividad_Exitoso() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(actividadRepository.findByProcesoIdAndActivaTrueOrderByOrdenAsc(1L)).thenReturn(List.of(actividad));

        actividadService.eliminarActividad(1L, 1L);

        assertFalse(actividad.getActiva());
        verify(actividadRepository, times(1)).save(any(Actividad.class));
        verify(actividadRepository, times(1)).saveAll(anyList());
    }

    @Test
    void eliminarActividad_FalloPermiso() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(usuarioService.obtenerUsuarioEntity(2L)).thenReturn(normalUsuario);

        assertThrows(BusinessRuleException.class, () -> actividadService.eliminarActividad(1L, 2L));
    }

    @Test
    void listarPorProceso_Exitoso() {
        when(actividadRepository.findByProcesoIdAndActivaTrueOrderByOrdenAsc(1L)).thenReturn(List.of(actividad));
        when(modelMapper.map(any(), eq(ActividadRegistroDTO.class))).thenReturn(new ActividadRegistroDTO());

        List<ActividadRegistroDTO> list = actividadService.listarPorProceso(1L);

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void obtenerPorId_Exitoso() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(modelMapper.map(any(), eq(ActividadRegistroDTO.class))).thenReturn(new ActividadRegistroDTO());

        ActividadRegistroDTO res = actividadService.obtenerPorId(1L);
        assertNotNull(res);
    }

    @Test
    void existePorRolProceso_Exitoso() {
        when(actividadRepository.existsByRolProcesoId(1L)).thenReturn(true);
        assertTrue(actividadService.existePorRolProceso(1L));
    }

    @Test
    void obtenerActividadesPorRolProceso_Exitoso() {
        when(actividadRepository.findByRolProcesoId(1L)).thenReturn(List.of(actividad));
        List<Actividad> list = actividadService.obtenerActividadesPorRolProceso(1L);
        assertFalse(list.isEmpty());
    }
}
