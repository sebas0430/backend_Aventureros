package com.edu.javeriana.backend.service;

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
}