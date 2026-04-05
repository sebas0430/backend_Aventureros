package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
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
class PoolServiceTest {

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private IEmpresaService empresaService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PoolService poolService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private Pool pool;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Empresa");

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        pool = Pool.builder()
                .id(1L)
                .nombre("Pool 1")
                .empresa(empresa)
                .build();
    }

    @Test
    void crearPool_Exitoso() {
        PoolRegistroDTO dto = new PoolRegistroDTO();
        dto.setEmpresaId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Pool New");

        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(poolRepository.save(any(Pool.class))).thenReturn(pool);
        when(modelMapper.map(any(), eq(PoolRegistroDTO.class))).thenReturn(dto);

        PoolRegistroDTO res = poolService.crearPool(dto);

        assertNotNull(res);
        verify(poolRepository).save(any());
    }

    @Test
    void crearPool_NoAdmin() {
        adminUsuario.setRol("OBSERVADOR");
        PoolRegistroDTO dto = new PoolRegistroDTO();
        dto.setEmpresaId(1L);
        dto.setUsuarioId(1L);

        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        assertThrows(BusinessRuleException.class, () -> poolService.crearPool(dto));
    }

    @Test
    void editarPool_Exitoso() {
        PoolEdicionDTO dto = new PoolEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Pool Updated");

        when(poolRepository.findById(1L)).thenReturn(Optional.of(pool));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(poolRepository.save(any())).thenReturn(pool);
        when(modelMapper.map(any(), eq(PoolEdicionDTO.class))).thenReturn(dto);

        PoolEdicionDTO res = poolService.editarPool(1L, dto);
        assertNotNull(res);
        assertEquals("Pool Updated", pool.getNombre());
    }

    @Test
    void eliminarPool_Exitoso() {
        when(poolRepository.findById(1L)).thenReturn(Optional.of(pool));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        poolService.eliminarPool(1L, 1L);
        verify(poolRepository).delete(pool);
    }

    @Test
    void listarPoolsPorEmpresa_Exitoso() {
        when(empresaService.existeEmpresa(1L)).thenReturn(true);
        when(poolRepository.findByEmpresaId(1L)).thenReturn(List.of(pool));
        when(modelMapper.map(any(), eq(PoolRegistroDTO.class))).thenReturn(new PoolRegistroDTO());

        List<PoolRegistroDTO> list = poolService.listarPoolsPorEmpresa(1L);
        assertFalse(list.isEmpty());
    }
}
