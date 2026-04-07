package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.modelmapper.ModelMapper;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsuarioService implements IUsuarioService {

    private static final String USUARIO_NOT_FOUND = "Usuario no encontrado";

    private final UsuarioRepository usuarioRepository;
    private final IEmpresaService empresaService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          @Lazy IEmpresaService empresaService,
                          EmailService emailService,
                          ModelMapper modelMapper,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaService    = empresaService;
        this.emailService      = emailService;
        this.modelMapper       = modelMapper;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioRegistroDTO invitarUsuario(String correo, String password, String rol, Long empresaId) {
        // Checamos que no nos estén tratando de registrar a alguien que ya existe.
        if (usuarioRepository.findByUsername(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo asociado a una cuenta");
        }

        // Buscamos la empresa a la que va a pertenecer el nuevo integrante.
        Empresa empresa = empresaService.obtenerEmpresaEntity(empresaId);

        // Creamos el nuevo usuario y, SUPER IMPORTANTE, ciframos la contraseña.
        Usuario usuario = new Usuario();
        usuario.setUsername(correo);
        usuario.setPasswordHash(passwordEncoder.encode(password)); // ¡Aquí es donde pasa la magia del cifrado!
        usuario.setRol(rol);
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        // Lo guardamos en la base.
        Usuario guardado = usuarioRepository.save(usuario);

        // Le mandamos un mail avisándole que ya puede entrar.
        emailService.enviarInvitacion(correo, password, empresa.getNombre(), rol);

        log.info("Usuario invitado exitosamente: {} con rol {} para la empresa {}", correo, rol, empresa.getNombre());

        // Devolvemos los datos básicos para que el frente sepa que todo salió bien.
        UsuarioRegistroDTO response = modelMapper.map(guardado, UsuarioRegistroDTO.class);
        response.setCorreo(guardado.getUsername());
        response.setEmpresaId(guardado.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioLoginDTO iniciarSesion(String correo, String password) {
        // Buscamos al usuario por su correo.
        Usuario usuario = usuarioRepository.findByUsername(correo)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        // Comparamos la contraseña que nos mandan con el hash que tenemos guardado.
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) { 
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        // Si el usuario está castigado (inactivo), no lo dejamos pasar.
        if (usuario.getActivo() == null || !usuario.getActivo()) {
        throw new IllegalArgumentException("El usuario se encuentra inactivo");
        }   

        // Si todo está bien, armamos su objeto de sesión.
        UsuarioLoginDTO response = modelMapper.map(usuario, UsuarioLoginDTO.class);
        response.setCorreo(usuario.getUsername());
        response.setEmpresaId(usuario.getEmpresa().getId());
        
        log.info("Inicio de sesión exitoso para usuario: {}", correo);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRegistroDTO obtenerUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USUARIO_NOT_FOUND));

        UsuarioRegistroDTO response = modelMapper.map(usuario, UsuarioRegistroDTO.class);
        response.setCorreo(usuario.getUsername());
        response.setEmpresaId(usuario.getEmpresa().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRegistroDTO> listarPorEmpresa(Long empresaId) {
        if (!empresaService.existeEmpresa(empresaId)) {
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
                .toList();
    }

    @Override
    @Transactional
    public UsuarioRegistroDTO actualizarUsuario(Long id, String rol, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USUARIO_NOT_FOUND));

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
                .orElseThrow(() -> new ResourceNotFoundException(USUARIO_NOT_FOUND));
        usuarioRepository.delete(usuario);
        log.info("Usuario {} eliminado exitosamente", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional
    public Usuario guardarUsuarioEntity(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioEntity(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USUARIO_NOT_FOUND));
    }
}