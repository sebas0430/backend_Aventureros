package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IPoolService;
import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class PoolService implements IPoolService {

    private static final String POOL_NOT_FOUND = "Pool no encontrado";

    private final PoolRepository poolRepository;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public PoolService(PoolRepository poolRepository,
                       @Lazy IEmpresaService empresaService,
                       @Lazy IUsuarioService usuarioService,
                       ModelMapper modelMapper) {
        this.poolRepository    = poolRepository;
        this.empresaService    = empresaService;
        this.usuarioService    = usuarioService;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public PoolRegistroDTO crearPool(PoolRegistroDTO dto) {
        // Buscamos la empresa a la que va a pertenecer este nuevo contenedor (Pool).
        Empresa empresa = empresaService.obtenerEmpresaEntity(dto.getEmpresaId());

        // Checamos que el que lo crea sea el jefe (ADMIN).
        validarUsuarioAdministradorDeEmpresa(dto.getUsuarioId(), empresa.getId());

        // Creamos el Pool, que es como el marco principal donde dibujamos procesos.
        Pool pool = Pool.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .empresa(empresa)
                .build();

        // Lo guardamos en la base de datos.
        Pool guardado = poolRepository.save(pool);

        log.info("AUDITORIA: Usuario {} (ADMIN) registró el Nuevo Pool '{}' (ID={}) para la Empresa ID={}",
                dto.getUsuarioId(), guardado.getNombre(), guardado.getId(), empresa.getId());

        // Devolvemos la info mapeada.
        PoolRegistroDTO response = modelMapper.map(guardado, PoolRegistroDTO.class);
        response.setEmpresaId(guardado.getEmpresa().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public PoolEdicionDTO editarPool(Long id, PoolEdicionDTO dto) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(POOL_NOT_FOUND));

        validarUsuarioAdministradorDeEmpresa(dto.getUsuarioId(), pool.getEmpresa().getId());

        pool.setNombre(dto.getNombre());
        pool.setDescripcion(dto.getDescripcion());

        Pool actualizado = poolRepository.save(pool);

        log.info("AUDITORIA: Usuario {} (ADMIN) actualizó el Pool ID={} (Nuevo Nombre: '{}')",
                dto.getUsuarioId(), actualizado.getId(), actualizado.getNombre());

        PoolEdicionDTO response = modelMapper.map(actualizado, PoolEdicionDTO.class);
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarPool(Long id, Long usuarioId) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(POOL_NOT_FOUND));

        validarUsuarioAdministradorDeEmpresa(usuarioId, pool.getEmpresa().getId());

        poolRepository.delete(pool);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolRegistroDTO> listarPoolsPorEmpresa(Long empresaId) {
        if (!empresaService.existeEmpresa(empresaId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        return poolRepository.findByEmpresaId(empresaId)
                .stream()
                .map(pool -> {
                    PoolRegistroDTO dto = modelMapper.map(pool, PoolRegistroDTO.class);
                    dto.setEmpresaId(pool.getEmpresa().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public Pool guardarPoolEntity(Pool pool) {
        return poolRepository.save(pool);
    }

    @Override
    @Transactional(readOnly = true)
    public Pool obtenerPoolEntity(Long id) {
        return poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(POOL_NOT_FOUND));
    }

    private void validarUsuarioAdministradorDeEmpresa(Long usuarioId, Long empresaId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("No tienes el rol de ADMINISTRADOR_EMPRESA para realizar esta accion");
        }

        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessRuleException("No perteneces a la empresa de la cual intentas modificar el pool");
        }
    }
}