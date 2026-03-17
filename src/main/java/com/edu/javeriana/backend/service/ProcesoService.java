package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesoService implements IProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    private final com.edu.javeriana.backend.repository.HistorialProcesoRepository historialProcesoRepository;

    @Transactional
    public Proceso crearProceso(ProcesoRegistroDTO dto) {

        // buscar la empresa
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario autor = usuarioRepository.findById(dto.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario autor no encontrado"));

        Proceso proceso = new Proceso();
        proceso.setNombre(dto.getNombre());
        proceso.setDescripcion(dto.getDescripcion());
        proceso.setCategoria(dto.getCategoria());
        proceso.setEmpresa(empresa);
        proceso.setAutor(autor);

        // Nace explícitamente en estado BORRADOR
        proceso.setEstado(com.edu.javeriana.backend.model.EstadoProceso.BORRADOR);

        return procesoRepository.save(proceso);
    }

    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId) {
        return procesoRepository.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Proceso> listarPorAutor(Long autorId) {
        return procesoRepository.findByAutorId(autorId);
    }

    @Transactional
    public Proceso actualizarDefinicion(Long procesoId, String definicionJson) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado"));

        proceso.setDefinicionJson(definicionJson);
        return procesoRepository.save(proceso);
    }

    @Transactional
    public Proceso editarProceso(Long id, com.edu.javeriana.backend.dto.ProcesoEdicionDTO dto) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Usuario no encontrado"));

        // Validar permisos: solo el autor o un administrador pueden editar
        boolean esAutor = proceso.getAutor().getId().equals(usuario.getId());
        boolean esAdmin = "ADMINISTRADOR_EMPRESA".equals(usuario.getRol());

        if (!esAutor && !esAdmin) {
            throw new com.edu.javeriana.backend.exception.BusinessRuleException(
                    "No tienes permisos para editar este proceso. Solo el autor o un administrador pueden hacerlo.");
        }

        StringBuilder cambios = new StringBuilder();
        if (!proceso.getNombre().equals(dto.getNombre())) {
            cambios.append("Nombre cambiado de '").append(proceso.getNombre()).append("' a '").append(dto.getNombre())
                    .append("'. ");
            proceso.setNombre(dto.getNombre());
        }
        if (!proceso.getDescripcion().equals(dto.getDescripcion())) {
            cambios.append("Descripción actualizada. ");
            proceso.setDescripcion(dto.getDescripcion());
        }
        if (!proceso.getCategoria().equals(dto.getCategoria())) {
            cambios.append("Categoría cambiada de '").append(proceso.getCategoria()).append("' a '")
                    .append(dto.getCategoria()).append("'. ");
            proceso.setCategoria(dto.getCategoria());
        }

        if (cambios.length() > 0) {
            proceso = procesoRepository.save(proceso);

            com.edu.javeriana.backend.model.HistorialProceso historial = com.edu.javeriana.backend.model.HistorialProceso
                    .builder()
                    .proceso(proceso)
                    .usuario(usuario)
                    .accion("EDICION")
                    .detalle(cambios.toString().trim())
                    .build();

            historialProcesoRepository.save(historial);
        }

        return proceso;
    }

    @Transactional
    public void eliminarProceso(Long procesoId, Long usuarioId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new com.edu.javeriana.backend.exception.BusinessRuleException(
                    "Solo un administrador puede eliminar procesos.");
        }

        proceso.setEstado(com.edu.javeriana.backend.model.EstadoProceso.INACTIVO);
        proceso = procesoRepository.save(proceso);

        com.edu.javeriana.backend.model.HistorialProceso historial = com.edu.javeriana.backend.model.HistorialProceso
                .builder()
                .proceso(proceso)
                .usuario(usuario)
                .accion("ELIMINACION")
                .detalle("El proceso fue eliminado (estado cambiado a INACTIVO).")
                .build();
        historialProcesoRepository.save(historial);
    }

    @Transactional
    public Proceso cambiarEstado(Long procesoId, com.edu.javeriana.backend.model.EstadoProceso nuevoEstado,
            Long usuarioId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Proceso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.edu.javeriana.backend.exception.ResourceNotFoundException(
                        "Usuario no encontrado"));

        // Reglas de negocio básicas para cambio de estado:
        // Solo el ADMIN_EMPRESA puede aprobar/publicar procesos.
        if (nuevoEstado == com.edu.javeriana.backend.model.EstadoProceso.PUBLICADO) {
            if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
                throw new com.edu.javeriana.backend.exception.BusinessRuleException(
                        "Solo un administrador puede publicar procesos.");
            }
        }

        proceso.setEstado(nuevoEstado);
        return procesoRepository.save(proceso);
    }
}