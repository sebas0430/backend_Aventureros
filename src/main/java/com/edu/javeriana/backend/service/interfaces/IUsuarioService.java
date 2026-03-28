package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;
import com.edu.javeriana.backend.model.Usuario;

public interface IUsuarioService {
    Usuario invitarUsuario(UsuarioRegistroDTO dto);

    Usuario iniciarSesion(UsuarioLoginDTO dto);

    Usuario obtenerUsuarioPorId(Long id);

    boolean existeUsuarioPorUsername(String username);

    Usuario guardarUsuario(Usuario usuario);
}
