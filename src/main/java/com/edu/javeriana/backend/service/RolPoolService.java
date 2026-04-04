package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.RolPool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.AsignacionRolPoolRepository;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.repository.RolPoolRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RolPoolService implements IRolPoolService {

    private final RolPoolRepository rolPoolRepository;
    private final AsignacionRolPoolRepository asignacionRolPoolRepository;
    private final PoolRepository poolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public RolPoolService(RolPoolRepository rolPoolRepository,
                          AsignacionRolPoolRepository asignacionRolPoolRepository,
                          PoolRepository poolRepository,
                          UsuarioRepository usuarioRepository,
                          ModelMapper modelMapper) {
        this.rolPoolRepository           = rolPoolRepository;
        this.asignacionRolPoolRepository = asignacionRolPoolRepository;
        this.poolRepository              = poolRepository;
        this.usuarioRepository           = usuarioRepository;
        this.modelMapper                 = modelMapper;
    }

    @Override
    @Transactional
    public RolPoolRegistroDTO crearRol(RolPoolRegistroDTO dto) {
        Pool pool = poolRepository.findById(dto.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarPermisoGestionRoles(dto.getUsuarioId(), pool);

        if (rolPoolRepository.existsByPoolIdAndNombre(pool.getId(), dto.getNombre())) {
            throw new BusinessRuleException("Ya existe un rol con este nombre en este pool");
        }

        RolPool rol = RolPool.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .pool(pool)
                .permisoCrearProceso(dto.isPermisoCrearProceso())
                .permisoEditarProceso(dto.isPermisoEditarProceso())
                .permisoEliminarProceso(dto.isPermisoEliminarProceso())
                .permisoPublicarProceso(dto.isPermisoPublicarProceso())
                .permisoGestionarRoles(dto.isPermisoGestionarRoles())
                .build();

        RolPool guardado = rolPoolRepository.save(rol);
        log.info("AUDITORIA: Usuario {} creó un nuevo Rol '{}' en el Pool ID={}",
                dto.getUsuarioId(), guardado.getNombre(), pool.getId());

        RolPoolRegistroDTO response = modelMapper.map(guardado, RolPoolRegistroDTO.class);
        response.setPoolId(guardado.getPool().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public RolPoolRegistroDTO editarRol(Long id, RolPoolRegistroDTO dto) {
        RolPool rol = rolPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        validarPermisoGestionRoles(dto.getUsuarioId(), rol.getPool());

        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        rol.setPermisoCrearProceso(dto.isPermisoCrearProceso());
        rol.setPermisoEditarProceso(dto.isPermisoEditarProceso());
        rol.setPermisoEliminarProceso(dto.isPermisoEliminarProceso());
        rol.setPermisoPublicarProceso(dto.isPermisoPublicarProceso());
        rol.setPermisoGestionarRoles(dto.isPermisoGestionarRoles());

        RolPool actualizado = rolPoolRepository.save(rol);
        log.info("AUDITORIA: Usuario {} modificó los permisos/datos del Rol ID={}",
                dto.getUsuarioId(), actualizado.getId());

        RolPoolRegistroDTO response = modelMapper.map(actualizado, RolPoolRegistroDTO.class);
        response.setPoolId(actualizado.getPool().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarRol(Long id, Long usuarioSolicitanteId) {
        RolPool rol = rolPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        validarPermisoGestionRoles(usuarioSolicitanteId, rol.getPool());

        if (asignacionRolPoolRepository.existsByRolId(rol.getId())) {
            throw new BusinessRuleException(
                    "No se puede eliminar un rol si hay usuarios asignados a él. Reasigne a los usuarios primero.");
        }

        rolPoolRepository.delete(rol);
        log.info("AUDITORIA: Usuario {} eliminó el Rol ID={}", usuarioSolicitanteId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolPoolRegistroDTO> listarRolesPorPool(Long poolId, Long usuarioId) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!solicitante.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException("Permiso denegado: La empresa no coincide.");
        }

        return rolPoolRepository.findByPoolId(poolId)
                .stream()
                .map(r -> {
                    RolPoolRegistroDTO dto = modelMapper.map(r, RolPoolRegistroDTO.class);
                    dto.setPoolId(r.getPool().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AsignacionRolDTO asignarRolAUsuario(AsignacionRolDTO dto) {
        Pool pool = poolRepository.findById(dto.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarPermisoGestionRoles(dto.getUsuarioId(), pool);

        Usuario destinatario = usuarioRepository.findById(dto.getUsuarioDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destino no encontrado"));

        if (!destinatario.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException(
                    "No puedes asignar un rol a un usuario que no pertenece a tu misma empresa.");
        }

        RolPool rol = rolPoolRepository.findById(dto.getRolPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol de pool no encontrado"));

        if (!rol.getPool().getId().equals(pool.getId())) {
            throw new BusinessRuleException("El rol que intentas asignar no pertenece a este Pool.");
        }

        AsignacionRolPool asignacion = asignacionRolPoolRepository
                .findByUsuarioIdAndPoolId(destinatario.getId(), pool.getId())
                .orElse(new AsignacionRolPool());

        asignacion.setUsuario(destinatario);
        asignacion.setRol(rol);
        asignacion.setPool(pool);

        AsignacionRolPool guardada = asignacionRolPoolRepository.save(asignacion);
        log.info("AUDITORIA: Usuario {} le asignó el rol '{}' al usuario {} en el pool ID={}",
                dto.getUsuarioId(), rol.getNombre(), destinatario.getId(), pool.getId());

        AsignacionRolDTO response = new AsignacionRolDTO();
        response.setUsuarioDestinoId(guardada.getUsuario().getId());
        response.setRolPoolId(guardada.getRol().getId());
        response.setPoolId(guardada.getPool().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void desasignarRolAUsuario(Long usuarioDestinoId, Long poolId, Long usuarioId) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarPermisoGestionRoles(usuarioId, pool);

        AsignacionRolPool asignacion = asignacionRolPoolRepository
                .findByUsuarioIdAndPoolId(usuarioDestinoId, poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario destino no tiene un rol en este pool"));

        asignacionRolPoolRepository.delete(asignacion);
        log.info("AUDITORIA: Usuario {} desasignó rol al usuario {} en el pool ID={}",
                usuarioId, usuarioDestinoId, poolId);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionRolDTO obtenerAsignacionUsuario(Long usuarioDestinoId, Long poolId) {
        return asignacionRolPoolRepository.findByUsuarioIdAndPoolId(usuarioDestinoId, poolId)
                .map(a -> {
                    AsignacionRolDTO dto = new AsignacionRolDTO();
                    dto.setUsuarioDestinoId(a.getUsuario().getId());
                    dto.setRolPoolId(a.getRol().getId());
                    dto.setPoolId(a.getPool().getId());
                    return dto;
                })
                .orElse(null);
    }

    private void validarPermisoGestionRoles(Long usuarioId, Pool pool) {
        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado"));

        if (!solicitante.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException("No perteneces a la empresa de este pool.");
        }

        if ("ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) {
            return;
        }

        AsignacionRolPool asignacionActual = asignacionRolPoolRepository
                .findByUsuarioIdAndPoolId(solicitante.getId(), pool.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "No tienes permisos suficientes (Tampoco tienes ningún rol asignado en este pool)."));

        if (!asignacionActual.getRol().isPermisoGestionarRoles()) {
            throw new BusinessRuleException("Tu rol en este pool (" +
                    asignacionActual.getRol().getNombre() +
                    ") no tiene los privilegios de gestionar roles.");
        }
    }
}