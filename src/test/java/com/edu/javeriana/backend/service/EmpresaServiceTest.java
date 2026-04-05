package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IPoolService poolService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa empresa;
    private EmpresaRegistroDTO registroDTO;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Empresa");
        empresa.setNit("123456789");

        registroDTO = new EmpresaRegistroDTO();
        registroDTO.setNombre("Test Empresa");
        registroDTO.setNit("123456789");
        registroDTO.setCorreoContacto("admin@test.com");
        registroDTO.setPasswordAdmin("password");
    }

    @Test
    void registrarEmpresa_Exitoso() {
        when(empresaRepository.findByNit(any())).thenReturn(Optional.empty());
        when(usuarioService.existeUsuarioPorUsername(any())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        when(modelMapper.map(any(Empresa.class), eq(EmpresaRegistroDTO.class))).thenReturn(registroDTO);

        EmpresaRegistroDTO res = empresaService.registrarEmpresa(registroDTO);

        assertNotNull(res);
        verify(empresaRepository).save(any());
        verify(usuarioService).guardarUsuarioEntity(any(Usuario.class));
        verify(poolService).guardarPoolEntity(any(Pool.class));
    }

    @Test
    void registrarEmpresa_NitDuplicado() {
        when(empresaRepository.findByNit(any())).thenReturn(Optional.of(empresa));
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(registroDTO));
    }

    @Test
    void editarEmpresa_Exitoso() {
        EmpresaEdicionDTO edicionDTO = new EmpresaEdicionDTO();
        edicionDTO.setNombre("Nuevo Nombre");
        edicionDTO.setNit("123456789");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any())).thenReturn(empresa);
        when(modelMapper.map(any(), eq(EmpresaEdicionDTO.class))).thenReturn(edicionDTO);

        EmpresaEdicionDTO res = empresaService.editarEmpresa(1L, edicionDTO);
        assertNotNull(res);
        assertEquals("Nuevo Nombre", res.getNombre());
    }

    @Test
    void editarEmpresa_NitColision() {
        EmpresaEdicionDTO edicionDTO = new EmpresaEdicionDTO();
        edicionDTO.setNit("9999999");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.findByNit("9999999")).thenReturn(Optional.of(new Empresa()));

        assertThrows(BusinessRuleException.class, () -> empresaService.editarEmpresa(1L, edicionDTO));
    }

    @Test
    void obtenerEmpresa_Exitoso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(modelMapper.map(any(), eq(EmpresaRegistroDTO.class))).thenReturn(registroDTO);

        EmpresaRegistroDTO res = empresaService.obtenerEmpresa(1L);
        assertNotNull(res);
    }

    @Test
    void obtenerEmpresa_NoEncontrada() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> empresaService.obtenerEmpresa(1L));
    }

    @Test
    void listarEmpresas() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));
        when(modelMapper.map(any(), eq(EmpresaRegistroDTO.class))).thenReturn(registroDTO);

        List<EmpresaRegistroDTO> list = empresaService.listarEmpresas();
        assertFalse(list.isEmpty());
    }

    @Test
    void eliminarEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        empresaService.eliminarEmpresa(1L);
        verify(empresaRepository).delete(empresa);
    }
}
