package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class EmpresaService implements IEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PoolRepository poolRepository;
    private final ModelMapper modelMapper;

    public EmpresaService(EmpresaRepository empresaRepository,
                          UsuarioRepository usuarioRepository,
                          PoolRepository poolRepository,
                          ModelMapper modelMapper) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.poolRepository    = poolRepository;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public EmpresaRegistroDTO registrarEmpresa(EmpresaRegistroDTO dto) {
        if (empresaRepository.findByNit(dto.getNit()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese NIT");
        }
        if (usuarioRepository.findByUsername(dto.getCorreoContacto()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con este username");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(dto.getNombre());
        empresa.setNit(dto.getNit());
        empresa.setUsuarios(new ArrayList<>());

        Empresa guardada = empresaRepository.save(empresa);

        // Crear usuario administrador inicial de la empresa
        Usuario admin = new Usuario();
        admin.setUsername(dto.getCorreoContacto());
        // El password idealmente debe venir encriptado (acá se asume que se asigna o ya viene para guardarse)
        admin.setPasswordHash(dto.getPasswordAdmin());
        admin.setRol("ADMINISTRADOR_EMPRESA");
        admin.setActivo(true);
        admin.setEmpresa(guardada);

        usuarioRepository.save(admin);

        // Crear Pool por defecto para la empresa (HU-21)
        Pool poolDefault = Pool.builder()
                .nombre("Pool Principal - " + guardada.getNombre())
                .descripcion("Área de procesos principal creada por defecto para la organización.")
                .empresa(guardada)
                .build();
        poolRepository.save(poolDefault);

        // Mapear entidad → DTO existente
        EmpresaRegistroDTO response = modelMapper.map(guardada, EmpresaRegistroDTO.class);
        response.setCorreoContacto(admin.getUsername()); // Llenamos de vuelta para el response
        response.setPasswordAdmin(null); // No devuelve el password

        return response;
    }

    @Override
    @Transactional
    public EmpresaEdicionDTO editarEmpresa(Long id, EmpresaEdicionDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        // Si cambió el NIT, verificamos que no colisione
        if (!empresa.getNit().equals(dto.getNit()) && empresaRepository.findByNit(dto.getNit()).isPresent()) {
            throw new BusinessRuleException("Ya existe otra empresa con ese NIT");
        }

        empresa.setNombre(dto.getNombre());
        empresa.setNit(dto.getNit());

        Empresa guardada = empresaRepository.save(empresa);

        return modelMapper.map(guardada, EmpresaEdicionDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaRegistroDTO obtenerEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        EmpresaRegistroDTO dto = modelMapper.map(empresa, EmpresaRegistroDTO.class);
        dto.setPasswordAdmin(null); // No exponer el password
        return dto;
    }

    @Override
@Transactional(readOnly = true)
public List<EmpresaRegistroDTO> listarEmpresas() {
    return empresaRepository.findAll()
            .stream()
            .map(e -> {
                EmpresaRegistroDTO dto = modelMapper.map(e, EmpresaRegistroDTO.class);
                dto.setPasswordAdmin(null);
                return dto;
            })
            .collect(Collectors.toList());
}

    @Override
@Transactional
public void eliminarEmpresa(Long id) {
    Empresa empresa = empresaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    empresaRepository.delete(empresa);
}
}
