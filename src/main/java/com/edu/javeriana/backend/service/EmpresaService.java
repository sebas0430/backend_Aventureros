package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import com.edu.javeriana.backend.service.interfaces.IPoolService;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import org.springframework.context.annotation.Lazy;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;



@Slf4j
@Service
public class EmpresaService implements IEmpresaService {

    private static final String EMPRESA_NOT_FOUND = "Empresa no encontrada";

    private final EmpresaRepository empresaRepository;
    private final IUsuarioService usuarioService;
    private final IPoolService poolService;
    private final ModelMapper modelMapper;

    public EmpresaService(EmpresaRepository empresaRepository,
                          @Lazy IUsuarioService usuarioService,
                          @Lazy IPoolService poolService,
                          ModelMapper modelMapper) {
        this.empresaRepository = empresaRepository;
        this.usuarioService    = usuarioService;
        this.poolService       = poolService;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public EmpresaRegistroDTO registrarEmpresa(EmpresaRegistroDTO dto) {
        // Validamos que la empresa y el correo del admin no existan ya.
        if (empresaRepository.findByNit(dto.getNit()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese NIT");
        }
        if (usuarioService.existeUsuarioPorUsername(dto.getCorreoContacto())) {
            throw new IllegalArgumentException("Ya existe un usuario con este username");
        }

        // Creamos la empresa nueva.
        Empresa empresa = new Empresa();
        empresa.setNombre(dto.getNombre());
        empresa.setNit(dto.getNit());
        empresa.setUsuarios(new ArrayList<>());

        Empresa guardada = empresaRepository.save(empresa);

        // De una vez le creamos su usuario Administrador para que pueda entrar.
        Usuario admin = new Usuario();
        admin.setUsername(dto.getCorreoContacto());
        admin.setPasswordHash(dto.getPasswordAdmin()); // Aquí se guarda la clave del jefe.
        admin.setRol("ADMINISTRADOR_EMPRESA");
        admin.setActivo(true);
        admin.setEmpresa(guardada);

        usuarioService.guardarUsuarioEntity(admin);

        // Y le regalamos un Pool por defecto para que no empiece con el mapa vacío.
        Pool poolDefault = Pool.builder()
                .nombre("Pool Principal - " + guardada.getNombre())
                .descripcion("Área de procesos principal creada por defecto para la organización.")
                .empresa(guardada)
                .build();
        poolService.guardarPoolEntity(poolDefault);

        // Convertimos a DTO (quitando la clave por seguridad) y devolvemos.
        EmpresaRegistroDTO response = modelMapper.map(guardada, EmpresaRegistroDTO.class);
        response.setCorreoContacto(admin.getUsername()); 
        response.setPasswordAdmin(null); 

        return response;
    }

    @Override
    @Transactional
    public EmpresaEdicionDTO editarEmpresa(Long id, EmpresaEdicionDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EMPRESA_NOT_FOUND));

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
                .orElseThrow(() -> new ResourceNotFoundException(EMPRESA_NOT_FOUND));

        EmpresaRegistroDTO dto = modelMapper.map(empresa, EmpresaRegistroDTO.class);
        dto.setPasswordAdmin(null); // No exponer el password
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa obtenerEmpresaEntity(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EMPRESA_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmpresa(Long id) {
        return empresaRepository.existsById(id);
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
            .toList();
}

    @Override
@Transactional
public void eliminarEmpresa(Long id) {
    Empresa empresa = empresaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(EMPRESA_NOT_FOUND));
    empresaRepository.delete(empresa);
}
}
