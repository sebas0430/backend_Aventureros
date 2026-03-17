package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;

public interface IEmpresaService {
    Empresa registrarEmpresa(EmpresaRegistroDTO dto);
}
