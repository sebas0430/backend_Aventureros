package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // La contraseña se usa solo aquí, para enviar la invitación por correo
        emailService.enviarInvitacion(correo, password, empresa.getNombre(), rol);

        // Mapear entidad → DTO (sin contraseña)
        UsuarioRegistroDTO response = modelMapper.map(guardado, UsuarioRegistroDTO.class);
        response.setCorreo(guardado.getUsername());
        response.setEmpresaId(guardado.getEmpresa().getId());
        return response;
    }

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

        // Mapear entidad → DTO (sin contraseña)
        UsuarioLoginDTO response = modelMapper.map(usuario, UsuarioLoginDTO.class);
        response.setCorreo(usuario.getUsername());
        response.setEmpresaId(usuario.getEmpresa().getId());
        return response;
    }
}
