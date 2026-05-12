package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IRolPoolService;
import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.RolGlobal;
import com.edu.javeriana.backend.model.RolPool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.AsignacionRolPoolRepository;
import com.edu.javeriana.backend.repository.RolPoolRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import com.edu.javeriana.backend.service.interfaces.IPoolService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class RolPoolService implements IRolPoolService {

    private final RolPoolRepository rolPoolRepository;
    private final AsignacionRolPoolRepository asignacionRolPoolRepository;
    private final IPoolService poolService;
    private final IUsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public RolPoolService(RolPoolRepository rolPoolRepository,
                          AsignacionRolPoolRepository asignacionRolPoolRepository,
                          @Lazy IPoolService poolService,
                          @Lazy IUsuarioService usuarioService,
                          UsuarioRepository usuarioRepository,
                          ModelMapper modelMapper) {
        this.rolPoolRepository           = rolPoolRepository;
        this.asignacionRolPoolRepository = asignacionRolPoolRepository;
        this.poolService                 = poolService;
        this.usuarioService              = usuarioService;
        this.usuarioRepository           = usuarioRepository;
        this.modelMapper                 = modelMapper;
    }

    @Override
    @Transactional
    public RolPoolRegistroDTO crearRol(RolPoolRegistroDTO dto) {
        // Buscamos el Pool donde vamos a definir estos permisos especiales.
        Pool pool = poolService.obtenerPoolEntity(dto.getPoolId());

        // Validamos que el usuario tenga permiso de gestionar quién hace qué en este pool.
        validarPermisoGestionRoles(dto.getUsuarioId(), pool);

        // No dejamos que le pongan el mismo nombre a dos roles en el mismo pool.
        if (rolPoolRepository.existsByPoolIdAndNombre(pool.getId(), dto.getNombre())) {
            throw new BusinessRuleException("Ya existe un rol con este nombre en este pool");
        }

        // Creamos el Rol de Pool con todos sus interruptores de permisos (Crear, Editar, Borrar, etc.).
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

        // Lo guardamos.
        RolPool guardado = rolPoolRepository.save(rol);
        log.info("AUDITORIA: Usuario {} creó un nuevo Rol '{}' en el Pool ID={}",
                dto.getUsuarioId(), guardado.getNombre(), pool.getId());

        return modelMapper.map(guardado, RolPoolRegistroDTO.class);
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
    @Transactional
    public List<RolPoolRegistroDTO> listarRolesPorPool(Long poolId, Long usuarioId) {
        Pool pool = poolService.obtenerPoolEntity(poolId);

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!solicitante.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException("Permiso denegado: La empresa no coincide.");
        }

        List<RolPool> roles = rolPoolRepository.findByPoolId(poolId);

        // Si no hay roles (Pool antiguo o error en creación), creamos los de por defecto
        if (roles.isEmpty()) {
            log.info("El Pool ID={} no tiene roles. Creando predeterminados...", poolId);
            crearRolesPredeterminados(pool);
            roles = rolPoolRepository.findByPoolId(poolId);
        }

        return roles.stream()
                .map(r -> {
                    String nombreEsperado = "Administrador " + pool.getNombre();
                    if (r.isPermisoGestionarRoles() && !nombreEsperado.equals(r.getNombre())) {
                        if ("Administrador".equals(r.getNombre()) || 
                            "Administrador del Pool".equals(r.getNombre()) || 
                            pool.getNombre().equals(r.getNombre())) {
                            
                            r.setNombre(nombreEsperado);
                            rolPoolRepository.save(r);
                        }
                    }
                    RolPoolRegistroDTO dto = modelMapper.map(r, RolPoolRegistroDTO.class);
                    dto.setPoolId(r.getPool().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public AsignacionRolDTO asignarRolAUsuario(AsignacionRolDTO dto) {
        Pool pool = poolService.obtenerPoolEntity(dto.getPoolId());

        validarPermisoGestionRoles(dto.getUsuarioId(), pool);

        Usuario destinatario = usuarioService.obtenerUsuarioEntity(dto.getUsuarioDestinoId());

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

        // Derivar y actualizar el RolGlobal del usuario según los permisos del RolPool asignado.
        // Si el usuario ya es ADMINISTRADOR_EMPRESA global, no lo tocamos.
        if (!RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(destinatario.getRol())) {
            String rolGlobalDerivado = derivarRolGlobal(rol);
            destinatario.setRol(rolGlobalDerivado);
            usuarioRepository.save(destinatario);
            log.info("AUDITORIA: RolGlobal del usuario {} actualizado a '{}' por asignación del rol '{}' en pool ID={}",
                    destinatario.getId(), rolGlobalDerivado, rol.getNombre(), pool.getId());
        }

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
        Pool pool = poolService.obtenerPoolEntity(poolId);

        validarPermisoGestionRoles(usuarioId, pool);

        AsignacionRolPool asignacion = asignacionRolPoolRepository
                .findByUsuarioIdAndPoolId(usuarioDestinoId, poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario destino no tiene un rol en este pool"));

        // Al quitar el rol, revertir al usuario a SOLO_LECTURA (a menos que sea admin global)
        Usuario destinatario = asignacion.getUsuario();
        if (!RolGlobal.ADMINISTRADOR_EMPRESA.name().equals(destinatario.getRol())) {
            destinatario.setRol(RolGlobal.SOLO_LECTURA.name());
            usuarioRepository.save(destinatario);
            log.info("AUDITORIA: RolGlobal del usuario {} revertido a SOLO_LECTURA por desasignación en pool ID={}",
                    usuarioDestinoId, poolId);
        }

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

    @Override
    @Transactional
    public void crearRolesPredeterminados(Pool pool) {
        log.info("Creando roles predeterminados para el Pool ID={}", pool.getId());

        // 1. Administrador
        RolPool admin = RolPool.builder()
                .nombre("Administrador" + " " + pool.getNombre())
                .descripcion("Tiene control total sobre los procesos y roles dentro de este departamento.")
                .pool(pool)
                .permisoCrearProceso(true)
                .permisoEditarProceso(true)
                .permisoEliminarProceso(true)
                .permisoPublicarProceso(true)
                .permisoGestionarRoles(true)
                .build();

        // 2. Editor
        RolPool editor = RolPool.builder()
                .nombre("Editor")
                .descripcion("Puede crear y editar procesos, pero no puede eliminarlos ni gestionar roles.")
                .pool(pool)
                .permisoCrearProceso(true)
                .permisoEditarProceso(true)
                .permisoEliminarProceso(false)
                .permisoPublicarProceso(true)
                .permisoGestionarRoles(false)
                .build();

        // 3. Lector
        RolPool lector = RolPool.builder()
                .nombre("Lector")
                .descripcion("Solo puede visualizar los procesos de este departamento.")
                .pool(pool)
                .permisoCrearProceso(false)
                .permisoEditarProceso(false)
                .permisoEliminarProceso(false)
                .permisoPublicarProceso(false)
                .permisoGestionarRoles(false)
                .build();

        rolPoolRepository.saveAll(List.of(admin, editor, lector));
    }

    /**
     * Deriva el RolGlobal que debe tener un usuario según los permisos de su RolPool.
     *
     * Regla:
     *   permisoGestionarRoles = true  → ADMINISTRADOR_EMPRESA
     *   permisoCrearProceso   = true  → EDITOR
     *   sin permisos activos         → SOLO_LECTURA
     */
    private String derivarRolGlobal(RolPool rol) {
        if (rol.isPermisoGestionarRoles()) {
            return RolGlobal.ADMINISTRADOR_EMPRESA.name();
        }
        if (rol.isPermisoCrearProceso() || rol.isPermisoEditarProceso() || rol.isPermisoPublicarProceso()) {
            return RolGlobal.EDITOR.name();
        }
        return RolGlobal.SOLO_LECTURA.name();
    }

    private void validarPermisoGestionRoles(Long usuarioId, Pool pool) {
        Usuario solicitante = usuarioService.obtenerUsuarioEntity(usuarioId);

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