package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProcesoService implements IProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PoolRepository poolRepository;
    private final ProcesoCompartidoRepository procesoCompartidoRepository;
    private final AsignacionRolPoolRepository asignacionRolPoolRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final ModelMapper modelMapper;

    public ProcesoService(ProcesoRepository procesoRepository,
                          EmpresaRepository empresaRepository,
                          UsuarioRepository usuarioRepository,
                          PoolRepository poolRepository,
                          ProcesoCompartidoRepository procesoCompartidoRepository,
                          AsignacionRolPoolRepository asignacionRolPoolRepository,
                          HistorialProcesoRepository historialProcesoRepository,
                          ModelMapper modelMapper) {
        this.procesoRepository          = procesoRepository;
        this.empresaRepository          = empresaRepository;
        this.usuarioRepository          = usuarioRepository;
        this.poolRepository             = poolRepository;
        this.procesoCompartidoRepository = procesoCompartidoRepository;
        this.asignacionRolPoolRepository = asignacionRolPoolRepository;
        this.historialProcesoRepository  = historialProcesoRepository;
        this.modelMapper                = modelMapper;
    }

    @Override
    @Transactional
    public ProcesoRegistroDTO crearProceso(ProcesoRegistroDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario autor = usuarioRepository.findById(dto.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario autor no encontrado"));

        Proceso proceso = new Proceso();
        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setEmpresa(empresa);
        proceso.setAutor(autor);

        Pool poolAsignado;
        if (dto.getPoolId() != null) {
            poolAsignado = poolRepository.findById(dto.getPoolId())
                    .orElseThrow(() -> new IllegalArgumentException("Pool no encontrado"));
            if (!poolAsignado.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("El pool no pertenece a la misma empresa");
            }
        } else {
            poolAsignado = poolRepository.findFirstByEmpresaIdOrderByIdAsc(empresa.getId())
                    .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene ningún pool configurado"));
        }
        proceso.setPool(poolAsignado);

        validarPermisoDeRol(autor.getId(), poolAsignado.getId(), "CREAR");

        proceso.setEstado(EstadoProceso.BORRADOR);

        Proceso guardado = procesoRepository.save(proceso);

        ProcesoRegistroDTO response = modelMapper.map(guardado, ProcesoRegistroDTO.class);
        response.setEmpresaId(guardado.getEmpresa().getId());
        response.setAutorId(guardado.getAutor().getId());
        response.setPoolId(guardado.getPool().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId) {
        return procesoRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proceso> listarPorAutor(Long autorId) {
        return procesoRepository.findByAutorId(autorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Proceso obtenerProcesoPorId(Long id) {
        return procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proceso> filtrarProcesos(Long empresaId, String estadoStr, String categoria) {
        EstadoProceso estado = null;
        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                estado = EstadoProceso.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado no válido");
            }
        }
        String categoriaQuery = (categoria != null && !categoria.isBlank()) ? categoria : null;
        return procesoRepository.buscarConFiltros(empresaId, estado, categoriaQuery);
    }

    @Override
    @Transactional
    public ProcesoEdicionDTO actualizarDefinicion(Long procesoId, String definicionJson) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado"));

        proceso.setDefinicionJson(definicionJson);
        Proceso actualizado = procesoRepository.save(proceso);

        ProcesoEdicionDTO response = modelMapper.map(actualizado, ProcesoEdicionDTO.class);
        return response;
    }

    @Override
    @Transactional
    public ProcesoEdicionDTO editarProceso(Long id, ProcesoEdicionDTO dto) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        validarPermisoDeRol(dto.getUsuarioId(), proceso.getPool().getId(), "EDITAR");

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean esAutor = proceso.getAutor().getId().equals(usuario.getId());
        boolean esAdmin = "ADMINISTRADOR_EMPRESA".equals(usuario.getRol());

        if (!esAutor && !esAdmin) {
            throw new BusinessRuleException(
                    "No tienes permisos para editar este proceso. Solo el autor o un administrador pueden hacerlo.");
        }

        StringBuilder cambios = new StringBuilder();
        if (!proceso.getNombre().equals(dto.getNombre())) {
            cambios.append("Nombre cambiado de '").append(proceso.getNombre()).append("' a '").append(dto.getNombre()).append("'. ");
            proceso.setNombre(dto.getNombre());
        }
        if (!proceso.getDescripcion().equals(dto.getDescripcion())) {
            cambios.append("Descripción actualizada. ");
            proceso.setDescripcion(dto.getDescripcion());
        }
        if (!proceso.getCategoria().equals(dto.getCategoria())) {
            cambios.append("Categoría cambiada de '").append(proceso.getCategoria()).append("' a '").append(dto.getCategoria()).append("'. ");
            proceso.setCategoria(dto.getCategoria());
        }

        if (cambios.length() > 0) {
            proceso = procesoRepository.save(proceso);
            HistorialProceso historial = HistorialProceso.builder()
                    .proceso(proceso).usuario(usuario)
                    .accion("EDICION").detalle(cambios.toString().trim())
                    .build();
            historialProcesoRepository.save(historial);
        }

        ProcesoEdicionDTO response = modelMapper.map(proceso, ProcesoEdicionDTO.class);
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarProceso(Long procesoId, Long usuarioId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        validarPermisoDeRol(usuarioId, proceso.getPool().getId(), "ELIMINAR");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede eliminar procesos.");
        }

        proceso.setEstado(EstadoProceso.INACTIVO);
        proceso = procesoRepository.save(proceso);

        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso).usuario(usuario)
                .accion("ELIMINACION").detalle("El proceso fue eliminado (estado cambiado a INACTIVO).")
                .build();
        historialProcesoRepository.save(historial);
    }

    @Override
    @Transactional
    public ProcesoEdicionDTO cambiarEstado(Long procesoId, EstadoProceso nuevoEstado, Long usuarioId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        if (nuevoEstado == EstadoProceso.PUBLICADO) {
            validarPermisoDeRol(usuarioId, proceso.getPool().getId(), "PUBLICAR");
        } else {
            validarPermisoDeRol(usuarioId, proceso.getPool().getId(), "EDITAR");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (nuevoEstado == EstadoProceso.PUBLICADO && !"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede publicar procesos.");
        }

        proceso.setEstado(nuevoEstado);
        Proceso actualizado = procesoRepository.save(proceso);

        ProcesoEdicionDTO response = modelMapper.map(actualizado, ProcesoEdicionDTO.class);
        response.setUsuarioId(usuarioId);
        return response;
    }

    @Override
    @Transactional
    public void compartirProceso(Long procesoId, ProcesoCompartirDTO dto) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol()) || !usuario.getEmpresa().getId().equals(proceso.getEmpresa().getId())) {
            throw new BusinessRuleException("Solo un administrador global de la empresa dueña puede compartir el proceso");
        }

        Pool poolDestino = poolRepository.findById(dto.getPoolDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool destino no encontrado"));

        if (procesoCompartidoRepository.findByProcesoIdAndPoolDestinoId(procesoId, poolDestino.getId()).isPresent()) {
            throw new BusinessRuleException("El proceso ya está compartido con este Pool");
        }

        ProcesoCompartido comparticion = ProcesoCompartido.builder()
                .proceso(proceso).poolDestino(poolDestino).permiso(dto.getPermiso())
                .build();
        procesoCompartidoRepository.save(comparticion);

        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso).usuario(usuario).accion("COMPARTIR")
                .detalle("Proceso compartido con el Pool ID: " + poolDestino.getId() + " con permiso " + dto.getPermiso().name())
                .build();
        historialProcesoRepository.save(historial);
    }

    @Override
    @Transactional
    public void quitarComparticionProceso(Long procesoId, Long poolDestinoId, Long usuarioId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol()) || !usuario.getEmpresa().getId().equals(proceso.getEmpresa().getId())) {
            throw new BusinessRuleException("Solo un administrador de la empresa dueña puede quitar la compartición");
        }

        procesoCompartidoRepository.deleteByProcesoIdAndPoolDestinoId(procesoId, poolDestinoId);

        HistorialProceso historial = HistorialProceso.builder()
                .proceso(proceso).usuario(usuario).accion("QUITAR_COMPARTICION")
                .detalle("Se revocó el acceso al proceso para el Pool ID: " + poolDestinoId)
                .build();
        historialProcesoRepository.save(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proceso> listarProcesosCompartidosConPool(Long poolId, Long usuarioId) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmpresa().getId().equals(pool.getEmpresa().getId())) {
            throw new BusinessRuleException("No perteneces a la empresa de este pool");
        }

        return procesoCompartidoRepository.findByPoolDestinoId(poolId)
                .stream()
                .map(ProcesoCompartido::getProceso)
                .toList();
    }

    private void validarPermisoDeRol(Long usuarioId, Long poolId, String accion) {
        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if ("ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) return;

        AsignacionRolPool asignacion = asignacionRolPoolRepository.findByUsuarioIdAndPoolId(usuarioId, poolId)
                .orElseThrow(() -> new BusinessRuleException("No cuentas con ningún rol asignado en este pool/departamento."));

        RolPool rol = asignacion.getRol();

        switch (accion) {
            case "CREAR"    -> { if (!rol.isPermisoCrearProceso())    throw new BusinessRuleException("Tu rol en este pool no permite CREAR procesos"); }
            case "EDITAR"   -> { if (!rol.isPermisoEditarProceso())   throw new BusinessRuleException("Tu rol en este pool no permite EDITAR procesos"); }
            case "ELIMINAR" -> { if (!rol.isPermisoEliminarProceso()) throw new BusinessRuleException("Tu rol en este pool no permite ELIMINAR procesos"); }
            case "PUBLICAR" -> { if (!rol.isPermisoPublicarProceso()) throw new BusinessRuleException("Tu rol en este pool no permite PUBLICAR procesos"); }
        }
    }
}