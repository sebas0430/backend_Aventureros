package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class EmpresaService implements IEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Empresa registrarEmpresa(EmpresaRegistroDTO dto) {
        if (empresaRepository.findByNit(dto.getNit()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese NIT");
        }
        if (usuarioRepository.findByUsername(dto.getCorreoContacto()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este username");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(dto.getNombre());
        empresa.setNit(dto.getNit());
        //empresa.setCorreoContacto(dto.getCorreoContacto());
        empresa.setUsuarios(new ArrayList<>());

        empresa = empresaRepository.save(empresa);

        //Crear usuario administrador inicial de la empresa
        Usuario admin = new Usuario();
        admin.setUsername(dto.getCorreoContacto());
        admin.setPasswordHash(dto.getPasswordAdmin());
        admin.setRol("ADMINISTRADOR_EMPRESA");
        admin.setActivo(true);
        admin.setEmpresa(empresa);

        usuarioRepository.save(admin);

        return empresa;
    }
}
