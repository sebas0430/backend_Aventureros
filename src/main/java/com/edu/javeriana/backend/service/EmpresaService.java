package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.RolGlobal;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Empresa registrarEmpresa(EmpresaRegistroDTO dto) {
        if (empresaRepository.findByNit(dto.getNit()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese NIT");
        }
        if (usuarioRepository.findByCorreo(dto.getCorreoContacto()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo electrónico");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(dto.getNombre());
        empresa.setNit(dto.getNit());
        //empresa.setCorreoContacto(dto.getCorreoContacto());
        empresa.setUsuarios(new ArrayList<>());

        empresa = empresaRepository.save(empresa);

        Usuario admin = new Usuario();
        admin.setCorreo(dto.getCorreoContacto());
        // TODO: Encriptar contraseña en futuros sprints (Spring Security)
        admin.setPassword(dto.getPasswordAdmin());
        admin.setRolGlobal(RolGlobal.ADMINISTRADOR_EMPRESA);
        admin.setEmpresa(empresa);

        usuarioRepository.save(admin);

        return empresa;
    }
}
