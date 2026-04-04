package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.AsignacionRolDTO;
import com.edu.javeriana.backend.dto.RolPoolRegistroDTO;

import java.util.List;

public interface IRolPoolService {

    RolPoolRegistroDTO crearRol(RolPoolRegistroDTO dto);

    RolPoolRegistroDTO editarRol(Long id, RolPoolRegistroDTO dto);

    void eliminarRol(Long id, Long usuarioSolicitanteId);

    List<RolPoolRegistroDTO> listarRolesPorPool(Long poolId, Long usuarioId);

    AsignacionRolDTO asignarRolAUsuario(AsignacionRolDTO dto);

    void desasignarRolAUsuario(Long usuarioDestinoId, Long poolId, Long usuarioId);

    AsignacionRolDTO obtenerAsignacionUsuario(Long usuarioDestinoId, Long poolId);
}
