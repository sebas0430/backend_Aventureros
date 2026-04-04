package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;

import java.util.List;

public interface IPoolService {

    PoolRegistroDTO crearPool(PoolRegistroDTO dto);
 
    PoolEdicionDTO editarPool(Long id, PoolEdicionDTO dto);
 
    void eliminarPool(Long id, Long usuarioId);
 
    List<PoolRegistroDTO> listarPoolsPorEmpresa(Long empresaId);
}
