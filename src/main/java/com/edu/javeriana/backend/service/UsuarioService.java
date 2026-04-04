package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmpresaRepository empresaRepository,
                          EmailService emailService,
                          ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.emailService      = emailService;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public UsuarioRegistroDTO invitarUsuario(String correo, String password, String rol, Long empresaId) {
        if (usuarioRepository.findByUsername(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo asociado a una cuenta");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario usuario = new Usuario();
        usuario.setUsername(correo);
        usuario.setPasswordHash(password);
        usuario.setRol(rol);
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);

        emailService.enviarInvitacion(correo, password, empresa.getNombre(), rol);

        UsuarioRegistroDTO response = modelMapper.map(guardado, UsuarioRegistroDTO.class);
        response.setCorreo(guardado.getUsername());
        response.setEmpresaId(guardado.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioLoginDTO iniciarSesion(String correo, String password) {
        Usuario usuario = usuarioRepository.findByUsername(correo)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!usuario.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario se encuentra inactivo");
        }

        UsuarioLoginDTO response = modelMapper.map(usuario, UsuarioLoginDTO.class);
        response.setCorreo(usuario.getUsername());
        response.setEmpresaId(usuario.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRegistroDTO obtenerUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UsuarioRegistroDTO response = modelMapper.map(usuario, UsuarioRegistroDTO.class);
        response.setCorreo(usuario.getUsername());
        response.setEmpresaId(usuario.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRegistroDTO> listarPorEmpresa(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }

        return usuarioRepository.findByEmpresaId(empresaId)
                .stream()
                .map(u -> {
                    UsuarioRegistroDTO dto = modelMapper.map(u, UsuarioRegistroDTO.class);
                    dto.setCorreo(u.getUsername());
                    dto.setEmpresaId(u.getEmpresa().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioRegistroDTO actualizarUsuario(Long id, String rol, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (rol != null)    usuario.setRol(rol);
        if (activo != null) usuario.setActivo(activo);

        Usuario actualizado = usuarioRepository.save(usuario);

        UsuarioRegistroDTO response = modelMapper.map(actualizado, UsuarioRegistroDTO.class);
        response.setCorreo(actualizado.getUsername());
        response.setEmpresaId(actualizado.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }
}