package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Lane;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.LaneRepository;
import com.edu.javeriana.backend.repository.PoolRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LaneService implements ILaneService {

    private final LaneRepository laneRepository;
    private final PoolRepository poolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public LaneService(LaneRepository laneRepository,
                       PoolRepository poolRepository,
                       UsuarioRepository usuarioRepository,
                       ModelMapper modelMapper) {
        this.laneRepository   = laneRepository;
        this.poolRepository   = poolRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper      = modelMapper;
    }

    @Override
    @Transactional
    public LaneRegistroDTO crearLane(LaneRegistroDTO dto) {
        Pool pool = poolRepository.findById(dto.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarAccesoYManejoDeLane(dto.getUsuarioId(), pool.getEmpresa().getId(), true);

        Lane lane = Lane.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .pool(pool)
                .build();

        Lane laneGuardado = laneRepository.save(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) registró el Nuevo Lane '{}' (ID={}) dentro del Pool ID={}",
                dto.getUsuarioId(), laneGuardado.getNombre(), laneGuardado.getId(), pool.getId());

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
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool no encontrado"));

        validarAccesoYManejoDeLane(usuarioId, pool.getEmpresa().getId(), false);

        return laneRepository.findByPoolId(poolId)
                .stream()
                .map(lane -> {
                    LaneRegistroDTO dto = modelMapper.map(lane, LaneRegistroDTO.class);
                    dto.setPoolId(lane.getPool().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validarAccesoYManejoDeLane(Long usuarioId, Long empresaPoolId, boolean requiresAdmin) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmpresa().getId().equals(empresaPoolId)) {
            throw new BusinessRuleException("No perteneces a la empresa de la cual intentas consultar o modificar este Lane");
        }

        if (requiresAdmin && !"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("No tienes el rol de ADMINISTRADOR_EMPRESA para modificar los Lanes (roles/departamentos) de los pools");
        }
    }
}