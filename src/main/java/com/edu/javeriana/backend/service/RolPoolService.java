package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.RolPool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.AsignacionRolPoolRepository;
import com.edu.javeriana.backend.repository.RolPoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolPoolService implements IRolPoolService {

    private final RolPoolRepository rolPoolRepository;
    private final AsignacionRolPoolRepository asignacionRolPoolRepository;
    private final @Lazy IPoolService poolService;
    private final @Lazy IUsuarioService usuarioService;

    @Override
    @Transactional
    public RolPool crearRol(RolPoolRegistroDTO dto) {
        Pool pool = poolService.obtenerPoolPorId(dto.getPoolId());

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

        log.info("AUDITORIA: Usuario {} creó un nuevo Rol '{}' en el Pool ID={}", dto.getUsuarioId(), rol.getNombre(),
                pool.getId());
        return rolPoolRepository.save(rol);
    }

    @Override
    @Transactional
    public RolPool editarRol(Long id, RolPoolRegistroDTO dto) {
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

        log.info("AUDITORIA: Usuario {} modificó los permisos/datos del Rol ID={}", dto.getUsuarioId(), rol.getId());
        return rolPoolRepository.save(rol);
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
    public List<RolPool> listarRolesPorPool(Long poolId, Long usuarioId) {
        Pool pool = poolService.obtenerPoolPorId(poolId);
        Usuario solicitante = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!solicitante.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException("Permiso denegado: La empresa no coincide.");
        }

        return rolPoolRepository.findByPoolId(poolId);
    }

    @Override
    @Transactional
    public AsignacionRolPool asignarRolAUsuario(AsignacionRolDTO dto) {
        Pool pool = poolService.obtenerPoolPorId(dto.getPoolId());

        validarPermisoGestionRoles(dto.getUsuarioId(), pool);

        Usuario destinatario = usuarioService.obtenerUsuarioPorId(dto.getUsuarioDestinoId());

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

        log.info("AUDITORIA: Usuario {} le asignó el rol '{}' al usuario {} en el pool ID={}", dto.getUsuarioId(),
                rol.getNombre(), destinatario.getId(), pool.getId());
        return asignacionRolPoolRepository.save(asignacion);
    }

    @Override
    @Transactional
    public void desasignarRolAUsuario(Long usuarioDestinoId, Long poolId, Long usuarioId) {
        Pool pool = poolService.obtenerPoolPorId(poolId);

        validarPermisoGestionRoles(usuarioId, pool);

        AsignacionRolPool asignacion = asignacionRolPoolRepository.findByUsuarioIdAndPoolId(usuarioDestinoId, poolId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario destino no tiene un rol en este pool"));

        asignacionRolPoolRepository.delete(asignacion);
        log.info("AUDITORIA: Usuario {} desasignó rol al usuario {} en el pool ID={}", usuarioId, usuarioDestinoId,
                poolId);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionRolPool obtenerAsignacionUsuario(Long usuarioDestinoId, Long poolId) {
        return asignacionRolPoolRepository.findByUsuarioIdAndPoolId(usuarioDestinoId, poolId).orElse(null);
    }

    private void validarPermisoGestionRoles(Long usuarioId, Pool pool) {
        Usuario solicitante = usuarioService.obtenerUsuarioPorId(usuarioId);

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
            throw new BusinessRuleException("Tu rol en este pool (" + asignacionActual.getRol().getNombre()
                    + ") no tiene los privilegios de gestionar roles.");
        }
    }
}
