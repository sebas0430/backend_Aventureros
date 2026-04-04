package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;
import com.edu.javeriana.backend.model.AsignacionRolPool;
import com.edu.javeriana.backend.model.RolPool;

import java.util.List;

public interface IRolPoolService {

    RolPool crearRol(RolPoolRegistroDTO dto);

    RolPool editarRol(Long id, RolPoolRegistroDTO dto);

    void eliminarRol(Long id, Long usuarioSolicitanteId);

    List<RolPool> listarRolesPorPool(Long poolId, Long usuarioId);

    AsignacionRolPool asignarRolAUsuario(AsignacionRolDTO dto);

    void desasignarRolAUsuario(Long usuarioDestinoId, Long poolId, Long usuarioId);

    AsignacionRolPool obtenerAsignacionUsuario(Long usuarioDestinoId, Long poolId);
}
