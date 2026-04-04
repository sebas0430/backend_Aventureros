package com.edu.javeriana.backend.service.interfaces;

import com.edu.javeriana.backend.dto.UsuarioLoginDTO;
import com.edu.javeriana.backend.dto.UsuarioRegistroDTO;

import java.util.List;

public interface IUsuarioService {

    UsuarioRegistroDTO invitarUsuario(String correo, String password, String rol, Long empresaId);

    UsuarioLoginDTO iniciarSesion(String correo, String password);

    UsuarioRegistroDTO obtenerUsuario(Long id);

    List<UsuarioRegistroDTO> listarPorEmpresa(Long empresaId);

    UsuarioRegistroDTO actualizarUsuario(Long id, String rol, Boolean activo);

    void eliminarUsuario(Long id);

    boolean existeUsuarioPorUsername(String username);

    com.edu.javeriana.backend.model.Usuario guardarUsuarioEntity(com.edu.javeriana.backend.model.Usuario usuario);

    com.edu.javeriana.backend.model.Usuario obtenerUsuarioEntity(Long id);
}