package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;

import java.util.List;

public interface IPoolService {

    PoolRegistroDTO crearPool(PoolRegistroDTO dto);
 
    PoolEdicionDTO editarPool(Long id, PoolEdicionDTO dto);
 
    void eliminarPool(Long id, Long usuarioId);
 
    List<PoolRegistroDTO> listarPoolsPorEmpresa(Long empresaId);

    com.edu.javeriana.backend.model.Pool guardarPoolEntity(com.edu.javeriana.backend.model.Pool pool);

    com.edu.javeriana.backend.model.Pool obtenerPoolEntity(Long id);
}
