package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;

import java.util.List;

public interface IRolProcesoService {

    RolProcesoRegistroDTO crearRolProceso(RolProcesoRegistroDTO dto);

    RolProcesoEdicionDTO editarRolProceso(Long id, RolProcesoEdicionDTO dto);

    List<RolProcesoRegistroDTO> listarRolesPorEmpresa(Long empresaId, Long usuarioId);

    void eliminarRolProceso(Long id, Long usuarioId);

    RolProcesoRegistroDTO obtenerRolProcesoPorId(Long id);

    List<RolProcesoDetalleDTO> consultarRolesConDetalle(Long empresaId, Long usuarioId);

    RolProcesoDetalleDTO consultarRolProcesoDetalle(Long id);
}