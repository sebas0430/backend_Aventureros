package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoolService implements IPoolService {

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Pool crearPool(PoolRegistroDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        validarUsuarioAdministradorDeEmpresa(dto.getUsuarioId(), empresa.getId());

        Pool pool = Pool.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .empresa(empresa)
                .build();

        Pool guardado = poolRepository.save(pool);

        log.info("AUDITORIA: Usuario {} (ADMIN) registró el Nuevo Pool '{}' (ID={}) para la Empresa ID={}",
                dto.getUsuarioId(), guardado.getNombre(), guardado.getId(), empresa.getId());

        return guardado;
    }

    @Override
    @Transactional
    public Pool editarPool(Long id, PoolEdicionDTO dto) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarUsuarioAdministradorDeEmpresa(dto.getUsuarioId(), pool.getEmpresa().getId());

        pool.setNombre(dto.getNombre());
        pool.setDescripcion(dto.getDescripcion());

        Pool actualizado = poolRepository.save(pool);

        log.info("AUDITORIA: Usuario {} (ADMIN) actualizó el Pool ID={} (Nuevo Nombre: '{}')",
                dto.getUsuarioId(), actualizado.getId(), actualizado.getNombre());

        return actualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public Pool obtenerPoolPorId(Long id) {
        return poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));
    }

    @Override
    @Transactional
    public void eliminarPool(Long id, Long usuarioId) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarUsuarioAdministradorDeEmpresa(usuarioId, pool.getEmpresa().getId());
        poolRepository.delete(pool);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pool> listarPoolsPorEmpresa(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        return poolRepository.findByEmpresaId(empresaId);
    }

    private void validarUsuarioAdministradorDeEmpresa(Long usuarioId, Long empresaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("No tienes el rol de ADMINISTRADOR_EMPRESA para realizar esta accion");
        }

        if (!usuario.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessRuleException("No perteneces a la empresa de la cual intentas modificar el pool");
        }
    }

    @Override
    @Transactional
    public Pool guardarPool(Pool pool) {
        return poolRepository.save(pool);
    }
}
