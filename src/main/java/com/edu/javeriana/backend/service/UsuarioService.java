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
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public Usuario invitarUsuario(UsuarioRegistroDTO dto) {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este username asociado a una cuenta");
        }

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());

        // TODO: En futuros sprints, la clave será generada/reestablecida por el usuario
        // invitado
        usuario.setPasswordHash(dto.getPasswordHash());
        usuario.setRol(dto.getRol());
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }
}
