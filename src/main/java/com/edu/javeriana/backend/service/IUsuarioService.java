package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Usuario;

public interface IUsuarioService {
    Usuario invitarUsuario(UsuarioRegistroDTO dto);
    Usuario iniciarSesion(UsuarioLoginDTO dto);
}
