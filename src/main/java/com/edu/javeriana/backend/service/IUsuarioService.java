package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;

public interface IUsuarioService {
    UsuarioRegistroDTO invitarUsuario(String correo, String password, String rol, Long empresaId);
    UsuarioLoginDTO iniciarSesion(String correo, String password);
}
