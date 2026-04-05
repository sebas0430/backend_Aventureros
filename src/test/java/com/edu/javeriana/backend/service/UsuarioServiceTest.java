package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IEmpresaService empresaService;

    @Mock
    private EmailService emailService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Empresa");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("test@test.com");
        usuario.setPasswordHash("pass");
        usuario.setRol("EDITOR");
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);
    }

    @Test
    void invitarUsuario_Exitoso() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioRepository.save(any())).thenReturn(usuario);
        
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setCorreo("test@test.com");
        dto.setEmpresaId(1L);
        when(modelMapper.map(any(), eq(UsuarioRegistroDTO.class))).thenReturn(dto);

        UsuarioRegistroDTO res = usuarioService.invitarUsuario("test@test.com", "pass", "EDITOR", 1L);

        assertNotNull(res);
        verify(emailService).enviarInvitacion(eq("test@test.com"), anyString(), anyString(), anyString());
        verify(usuarioRepository).save(any());
    }

    @Test
    void invitarUsuario_YaExiste() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.invitarUsuario("test@test.com", "p", "R", 1L));
    }

    @Test
    void iniciarSesion_Exitoso() {
        when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuario));
        
        UsuarioLoginDTO loginDto = new UsuarioLoginDTO();
        loginDto.setCorreo("test@test.com");
        loginDto.setEmpresaId(1L);
        when(modelMapper.map(any(), eq(UsuarioLoginDTO.class))).thenReturn(loginDto);

        UsuarioLoginDTO res = usuarioService.iniciarSesion("test@test.com", "pass");

        assertNotNull(res);
        assertEquals("test@test.com", res.getCorreo());
    }

    @Test
    void iniciarSesion_CredencialesInvalidas() {
        when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuario));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.iniciarSesion("test@test.com", "wrong"));
    }

    @Test
    void iniciarSesion_UsuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuario));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.iniciarSesion("test@test.com", "pass"));
    }

    @Test
    void obtenerUsuario_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        UsuarioRegistroDTO dto = new UsuarioRegistroDTO();
        dto.setCorreo(usuario.getUsername());
        dto.setEmpresaId(empresa.getId());
        when(modelMapper.map(any(), eq(UsuarioRegistroDTO.class))).thenReturn(dto);

        UsuarioRegistroDTO res = usuarioService.obtenerUsuario(1L);
        assertNotNull(res);
    }

    @Test
    void listarPorEmpresa_Exitoso() {
        when(empresaService.existeEmpresa(1L)).thenReturn(true);
        when(usuarioRepository.findByEmpresaId(1L)).thenReturn(List.of(usuario));
        when(modelMapper.map(any(), eq(UsuarioRegistroDTO.class))).thenReturn(new UsuarioRegistroDTO());

        List<UsuarioRegistroDTO> list = usuarioService.listarPorEmpresa(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void actualizarUsuario_Exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(modelMapper.map(any(), eq(UsuarioRegistroDTO.class))).thenReturn(new UsuarioRegistroDTO());

        UsuarioRegistroDTO res = usuarioService.actualizarUsuario(1L, "ADMIN", false);
        assertNotNull(res);
        assertEquals("ADMIN", usuario.getRol());
        assertFalse(usuario.getActivo());
    }

    @Test
    void eliminarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        usuarioService.eliminarUsuario(1L);
        verify(usuarioRepository).delete(usuario);
    }
}
