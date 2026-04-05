package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;

import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ArcoRepository;
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
class ArcoServiceTest {

    @Mock
    private ArcoRepository arcoRepository;

    @Mock
    private IProcesoService procesoService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ArcoService arcoService;

    private Proceso proceso;
    private Usuario adminUsuario;
    private Arco arco;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);
        Usuario autor = new Usuario();
        autor.setId(1L);
        proceso.setAutor(autor);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");

        arco = Arco.builder()
                .id(1L)
                .proceso(proceso)
                .origenId(1L)
                .origenTipo(TipoNodo.ACTIVIDAD)
                .destinoId(2L)
                .destinoTipo(TipoNodo.GATEWAY)
                .build();
    }

    @Test
    void crearArco_Exitoso() {
        ArcoRegistroDTO dto = new ArcoRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setOrigenId(1L);
        dto.setOrigenTipo("ACTIVIDAD");
        dto.setDestinoId(2L);
        dto.setDestinoTipo("GATEWAY");

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(any(), any(), any(), any(), any())).thenReturn(false);
        when(arcoRepository.save(any(Arco.class))).thenReturn(arco);
        when(modelMapper.map(any(), eq(ArcoRegistroDTO.class))).thenReturn(dto);

        ArcoRegistroDTO res = arcoService.crearArco(dto);

        assertNotNull(res);
        verify(arcoRepository).save(any());
    }

    @Test
    void crearArco_MismoNodo() {
        ArcoRegistroDTO dto = new ArcoRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setOrigenId(1L);
        dto.setOrigenTipo("ACTIVIDAD");
        dto.setDestinoId(1L);
        dto.setDestinoTipo("ACTIVIDAD");

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        assertThrows(BusinessRuleException.class, () -> arcoService.crearArco(dto));
    }

    @Test
    void editarArco_Exitoso() {
        ArcoEdicionDTO dto = new ArcoEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setOrigenId(1L);
        dto.setOrigenTipo("ACTIVIDAD");
        dto.setDestinoId(3L);
        dto.setDestinoTipo("ACTIVIDAD");

        when(arcoRepository.findById(1L)).thenReturn(Optional.of(arco));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(arcoRepository.save(any())).thenReturn(arco);
        when(modelMapper.map(any(), eq(ArcoEdicionDTO.class))).thenReturn(dto);

        ArcoEdicionDTO res = arcoService.editarArco(1L, dto);
        assertNotNull(res);
    }

    @Test
    void listarArcosPorProceso_Exitoso() {
        when(procesoService.existeProceso(1L)).thenReturn(true);
        when(arcoRepository.findByProcesoId(1L)).thenReturn(List.of(arco));
        when(modelMapper.map(any(), eq(ArcoRegistroDTO.class))).thenReturn(new ArcoRegistroDTO());

        List<ArcoRegistroDTO> list = arcoService.listarArcosPorProceso(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void eliminarArco_Exitoso() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(arcoRepository.findById(1L)).thenReturn(Optional.of(arco));

        arcoService.eliminarArco(1L, 1L);
        verify(arcoRepository).delete(arco);
    }

    @Test
    void eliminarArcosPorNodo() {
        when(arcoRepository.findByProcesoIdAndOrigenIdAndOrigenTipo(1L, 1L, TipoNodo.ACTIVIDAD)).thenReturn(List.of(arco));
        when(arcoRepository.findByProcesoIdAndDestinoIdAndDestinoTipo(1L, 1L, TipoNodo.ACTIVIDAD)).thenReturn(Collections.emptyList());

        arcoService.eliminarArcosPorNodo(1L, 1L, TipoNodo.ACTIVIDAD);
        verify(arcoRepository, times(2)).deleteAll(any());
    }
}
