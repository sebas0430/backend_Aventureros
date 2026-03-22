package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.model.RolProceso;

import java.util.List;

public interface IRolProcesoService {

    RolProceso crearRolProceso(RolProcesoRegistroDTO dto);

    RolProceso editarRolProceso(Long id, RolProcesoEdicionDTO dto);

    List<RolProceso> listarRolesPorEmpresa(Long empresaId, Long usuarioId);

    void eliminarRolProceso(Long id, Long usuarioId);

    RolProceso obtenerRolProcesoPorId(Long id);
}
