package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.ILaneService;
import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Lane;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.LaneRepository;
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
public class LaneService implements ILaneService {

    private final LaneRepository laneRepository;
    private final IPoolService poolService;
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public LaneService(LaneRepository laneRepository,
                       @Lazy IPoolService poolService,
                       @Lazy IUsuarioService usuarioService,
                       ModelMapper modelMapper) {
        this.laneRepository   = laneRepository;
        this.poolService      = poolService;
        this.usuarioService   = usuarioService;
        this.modelMapper      = modelMapper;
    }

    @Override
    @Transactional
    public LaneRegistroDTO crearLane(LaneRegistroDTO dto) {
        // Buscamos el Pool (el contenedor grande) donde vamos a meter este carril.
        Pool pool = poolService.obtenerPoolEntity(dto.getPoolId());

        // Solo los administradores pueden crear carriles (áreas o departamentos).
        validarAccesoYManejoDeLane(dto.getUsuarioId(), pool.getEmpresa().getId(), true);

        // Creamos el carril (LANE) que representa una división de responsabilidad.
        Lane lane = Lane.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .pool(pool)
                .build();

        // Lo guardamos en la base de datos.
        Lane laneGuardado = laneRepository.save(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) registró el Nuevo Lane '{}' (ID={}) dentro del Pool ID={}",
                dto.getUsuarioId(), laneGuardado.getNombre(), laneGuardado.getId(), pool.getId());

        // Devolvemos el DTO con la info de quién lo creó y en qué pool quedó.
        LaneRegistroDTO response = modelMapper.map(laneGuardado, LaneRegistroDTO.class);
        response.setPoolId(laneGuardado.getPool().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public LaneEdicionDTO editarLane(Long id, LaneEdicionDTO dto) {
        Lane lane = laneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane no encontrado"));

        validarAccesoYManejoDeLane(dto.getUsuarioId(), lane.getPool().getEmpresa().getId(), true);

        lane.setNombre(dto.getNombre());
        lane.setDescripcion(dto.getDescripcion());

        Lane laneActualizado = laneRepository.save(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) actualizó el Lane ID={} (Nuevo Nombre: '{}')",
                dto.getUsuarioId(), laneActualizado.getId(), laneActualizado.getNombre());

        LaneEdicionDTO response = modelMapper.map(laneActualizado, LaneEdicionDTO.class);
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public void eliminarLane(Long id, Long usuarioId) {
        Lane lane = laneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane no encontrado"));

        validarAccesoYManejoDeLane(usuarioId, lane.getPool().getEmpresa().getId(), true);

        laneRepository.delete(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) eliminó el Lane ID={}", usuarioId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaneRegistroDTO> listarLanesPorPool(Long poolId, Long usuarioId) {
        Pool pool = poolService.obtenerPoolEntity(poolId);

        validarAccesoYManejoDeLane(usuarioId, pool.getEmpresa().getId(), false);

        return laneRepository.findByPoolId(poolId)
                .stream()
                .map(lane -> {
                    LaneRegistroDTO dto = modelMapper.map(lane, LaneRegistroDTO.class);
                    dto.setPoolId(lane.getPool().getId());
                    return dto;
                })
                .toList();
    }

    private void validarAccesoYManejoDeLane(Long usuarioId, Long empresaPoolId, boolean requiresAdmin) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!usuario.getEmpresa().getId().equals(empresaPoolId)) {
            throw new BusinessRuleException("No perteneces a la empresa de la cual intentas consultar o modificar este Lane");
        }

        if (requiresAdmin && !"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("No tienes el rol de ADMINISTRADOR_EMPRESA para modificar los Lanes (roles/departamentos) de los pools");
        }
    }
}