package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final EmailService emailService;

    @Transactional
    public Usuario invitarUsuario(UsuarioRegistroDTO dto) {
        if (usuarioRepository.findByUsername(dto.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo asociado a una cuenta");
        }

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getCorreo());
        usuario.setPasswordHash(dto.getPasswordHash());
        //se asigna el rol al usuario dentro de la empresa
        usuario.setRol(dto.getRol());
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        
        // Enviar invitación por correo
        // (El envío real dentro de EmailService está comentado hasta que pongas las credenciales)
        emailService.enviarInvitacion(
                usuario.getUsername(), 
                dto.getPasswordHash(), 
                empresa.getNombre(), 
                usuario.getRol()
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public Usuario iniciarSesion(com.edu.javeriana.backend.dto.UsuarioLoginDTO dto) {
        
        // En tu modelo el nombre del campo sigue siendo "username", aunque se le pase un correo
        Usuario usuario = usuarioRepository.findByUsername(dto.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));


        if (!usuario.getPasswordHash().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario se encuentra inactivo");
        }

        return usuario;
    }
}
