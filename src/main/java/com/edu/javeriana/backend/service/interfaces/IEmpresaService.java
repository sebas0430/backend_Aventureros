package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;

public interface IEmpresaService {
    Empresa registrarEmpresa(EmpresaRegistroDTO dto);

    Empresa obtenerEmpresaPorId(Long id);
}
