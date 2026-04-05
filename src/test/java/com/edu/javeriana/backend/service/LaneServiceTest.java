package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Lane;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.LaneRepository;
import com.edu.javeriana.backend.service.interfaces.IPoolService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private IPoolService poolService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LaneService laneService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private Pool pool;
    private Lane lane;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        pool = Pool.builder().id(1L).empresa(empresa).build();
        lane = Lane.builder().id(1L).nombre("Lane 1").pool(pool).build();
    }

    @Test
    void crearLane_Exitoso() {
        LaneRegistroDTO dto = new LaneRegistroDTO();
        dto.setPoolId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Lane X");

        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(laneRepository.save(any(Lane.class))).thenReturn(lane);
        when(modelMapper.map(any(), eq(LaneRegistroDTO.class))).thenReturn(dto);

        LaneRegistroDTO res = laneService.crearLane(dto);

        assertNotNull(res);
        verify(laneRepository).save(any());
    }

    @Test
    void crearLane_FalloEmpresaDiferente() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(2L);
        adminUsuario.setEmpresa(otraEmpresa);

        LaneRegistroDTO dto = new LaneRegistroDTO();
        dto.setPoolId(1L);
        dto.setUsuarioId(1L);

        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        assertThrows(BusinessRuleException.class, () -> laneService.crearLane(dto));
    }

    @Test
    void editarLane_Exitoso() {
        LaneEdicionDTO dto = new LaneEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Lane Updated");

        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(laneRepository.save(any())).thenReturn(lane);
        when(modelMapper.map(any(), eq(LaneEdicionDTO.class))).thenReturn(dto);

        LaneEdicionDTO res = laneService.editarLane(1L, dto);
        assertNotNull(res);
        assertEquals("Lane Updated", lane.getNombre());
    }

    @Test
    void eliminarLane_Exitoso() {
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        laneService.eliminarLane(1L, 1L);
        verify(laneRepository).delete(lane);
    }

    @Test
    void listarLanesPorPool_Exitoso() {
        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(laneRepository.findByPoolId(1L)).thenReturn(List.of(lane));
        when(modelMapper.map(any(), eq(LaneRegistroDTO.class))).thenReturn(new LaneRegistroDTO());

        List<LaneRegistroDTO> list = laneService.listarLanesPorPool(1L, 1L);
        assertFalse(list.isEmpty());
    }
}
