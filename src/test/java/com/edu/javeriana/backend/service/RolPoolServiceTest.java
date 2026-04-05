package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;

import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.RolPool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.AsignacionRolPoolRepository;
import com.edu.javeriana.backend.repository.RolPoolRepository;
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
class RolPoolServiceTest {

    @Mock
    private RolPoolRepository rolPoolRepository;
    @Mock
    private AsignacionRolPoolRepository asignacionRolPoolRepository;
    @Mock
    private IPoolService poolService;
    @Mock
    private IUsuarioService usuarioService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RolPoolService rolPoolService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private Pool pool;
    private RolPool rol;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        pool = Pool.builder().id(1L).empresa(empresa).build();

        rol = RolPool.builder()
                .id(1L)
                .nombre("Rol 1")
                .pool(pool)
                .build();
    }

    @Test
    void crearRol_Exitoso() {
        RolPoolRegistroDTO dto = new RolPoolRegistroDTO();
        dto.setPoolId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Nuevo Rol");

        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolPoolRepository.existsByPoolIdAndNombre(1L, "Nuevo Rol")).thenReturn(false);
        when(rolPoolRepository.save(any(RolPool.class))).thenReturn(rol);
        when(modelMapper.map(any(), eq(RolPoolRegistroDTO.class))).thenReturn(dto);

        RolPoolRegistroDTO res = rolPoolService.crearRol(dto);

        assertNotNull(res);
        verify(rolPoolRepository).save(any());
    }

    @Test
    void editarRol_Exitoso() {
        RolPoolRegistroDTO dto = new RolPoolRegistroDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Nombre Editado");

        when(rolPoolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolPoolRepository.save(any())).thenReturn(rol);
        when(modelMapper.map(any(), eq(RolPoolRegistroDTO.class))).thenReturn(dto);

        RolPoolRegistroDTO res = rolPoolService.editarRol(1L, dto);
        assertNotNull(res);
        assertEquals("Nombre Editado", rol.getNombre());
    }

    @Test
    void eliminarRol_Exitoso() {
        when(rolPoolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(asignacionRolPoolRepository.existsByRolId(1L)).thenReturn(false);

        rolPoolService.eliminarRol(1L, 1L);
        verify(rolPoolRepository).delete(rol);
    }

    @Test
    void asignarRolAUsuario_Exitoso() {
        AsignacionRolDTO dto = new AsignacionRolDTO();
        dto.setPoolId(1L);
        dto.setUsuarioId(1L);
        dto.setUsuarioDestinoId(2L);
        dto.setRolPoolId(1L);

        Usuario destino = new Usuario();
        destino.setId(2L);
        destino.setEmpresa(empresa);

        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(usuarioService.obtenerUsuarioEntity(2L)).thenReturn(destino);
        when(rolPoolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(asignacionRolPoolRepository.findByUsuarioIdAndPoolId(2L, 1L)).thenReturn(Optional.empty());
        
        AsignacionRolPool asignacion = new AsignacionRolPool();
        asignacion.setUsuario(destino);
        asignacion.setRol(rol);
        asignacion.setPool(pool);
        when(asignacionRolPoolRepository.save(any())).thenReturn(asignacion);

        AsignacionRolDTO res = rolPoolService.asignarRolAUsuario(dto);
        assertNotNull(res);
        assertEquals(2L, res.getUsuarioDestinoId());
    }

    @Test
    void desasignarRol_Exitoso() {
        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(asignacionRolPoolRepository.findByUsuarioIdAndPoolId(2L, 1L)).thenReturn(Optional.of(new AsignacionRolPool()));

        rolPoolService.desasignarRolAUsuario(2L, 1L, 1L);
        verify(asignacionRolPoolRepository).delete(any());
    }

    @Test
    void listarRolesPorPool_Exitoso() {
        when(poolService.obtenerPoolEntity(1L)).thenReturn(pool);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolPoolRepository.findByPoolId(1L)).thenReturn(List.of(rol));
        when(modelMapper.map(any(), eq(RolPoolRegistroDTO.class))).thenReturn(new RolPoolRegistroDTO());

        List<RolPoolRegistroDTO> list = rolPoolService.listarRolesPorPool(1L, 1L);
        assertFalse(list.isEmpty());
    }
}
