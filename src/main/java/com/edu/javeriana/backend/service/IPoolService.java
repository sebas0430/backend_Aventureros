package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.PoolEdicionDTO;
import com.edu.javeriana.backend.dto.PoolRegistroDTO;
import com.edu.javeriana.backend.model.Pool;

import java.util.List;

public interface IPoolService {

    Pool crearPool(PoolRegistroDTO dto);

    Pool editarPool(Long id, PoolEdicionDTO dto);

    void eliminarPool(Long id, Long usuarioId);

    List<Pool> listarPoolsPorEmpresa(Long empresaId);
}
