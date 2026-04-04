package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.LaneEdicionDTO;
import com.edu.javeriana.backend.dto.LaneRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Lane;
import com.edu.javeriana.backend.model.Pool;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.LaneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaneService implements ILaneService {

    private final LaneRepository laneRepository;
    private final @Lazy IPoolService poolService;
    private final @Lazy IUsuarioService usuarioService;

    @Override
    @Transactional
    public Lane crearLane(LaneRegistroDTO dto) {
        Pool pool = poolService.obtenerPoolPorId(dto.getPoolId());

        validarAccesoYManejoDeLane(dto.getUsuarioId(), pool.getEmpresa().getId(), true);

        Lane lane = Lane.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .pool(pool)
                .build();

        Lane laneGuardado = laneRepository.save(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) registró el Nuevo Lane '{}' (ID={}) dentro del Pool ID={}",
                dto.getUsuarioId(), laneGuardado.getNombre(), laneGuardado.getId(), pool.getId());

        return laneGuardado;
    }

    @Override
    @Transactional
    public Lane editarLane(Long id, LaneEdicionDTO dto) {
        Lane lane = laneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lane no encontrado"));

        validarAccesoYManejoDeLane(dto.getUsuarioId(), lane.getPool().getEmpresa().getId(), true);

        lane.setNombre(dto.getNombre());
        lane.setDescripcion(dto.getDescripcion());

        Lane laneActualizado = laneRepository.save(lane);

        log.info("AUDITORIA: Usuario {} (ADMIN) actualizó el Lane ID={} (Nuevo Nombre: '{}')",
                dto.getUsuarioId(), laneActualizado.getId(), laneActualizado.getNombre());

        return laneActualizado;
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
    public List<Lane> listarLanesPorPool(Long poolId, Long usuarioId) {
        Pool pool = poolService.obtenerPoolPorId(poolId);

        validarAccesoYManejoDeLane(usuarioId, pool.getEmpresa().getId(), false);

        return laneRepository.findByPoolId(poolId);
    }

    private void validarAccesoYManejoDeLane(Long usuarioId, Long empresaPoolId, boolean requiresAdmin) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!usuario.getEmpresa().getId().equals(empresaPoolId)) {
            throw new BusinessRuleException(
                    "No perteneces a la empresa de la cual intentas consultar o modificar este Lane");
        }

        if (requiresAdmin && !"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "No tienes el rol de ADMINISTRADOR_EMPRESA para modificar los Lanes (roles/departamentos) de los pools");
        }
    }
}
