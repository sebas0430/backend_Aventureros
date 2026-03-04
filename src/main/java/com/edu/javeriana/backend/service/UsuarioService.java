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
        if (usuarioRepository.findByCorreo(dto.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo asociado a una cuenta");
        }

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario usuario = new Usuario();
        usuario.setCorreo(dto.getCorreo());

        // TODO: En futuros sprints, la clave será generada/reestablecida por el usuario
        // invitado
        usuario.setPassword(dto.getPassword());
        usuario.setRolGlobal(dto.getRolGlobal());
        usuario.setEmpresa(empresa);

        return usuarioRepository.save(usuario);
    }
}
