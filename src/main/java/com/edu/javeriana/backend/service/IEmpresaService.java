package com.edu.javeriana.backend.service;

import java.util.List;

import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;

public interface IEmpresaService {

    EmpresaRegistroDTO registrarEmpresa(EmpresaRegistroDTO dto);

    EmpresaEdicionDTO editarEmpresa(Long id, EmpresaEdicionDTO dto);

    EmpresaRegistroDTO obtenerEmpresa(Long id);

    List<EmpresaRegistroDTO> listarEmpresas();

    void eliminarEmpresa(Long id);
}
