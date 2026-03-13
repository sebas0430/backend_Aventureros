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
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Proceso crearProceso(ProcesoRegistroDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario autor = usuarioRepository.findById(dto.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario autor no encontrado"));

        Proceso proceso = new Proceso();
        proceso.setTitulo(dto.getTitulo());
        proceso.setDefinicionJson(dto.getDefinicionJson());
        proceso.setEmpresa(empresa);
        proceso.setAutor(autor);

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
    public void eliminarProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new IllegalArgumentException("Proceso no encontrado");
        }
        procesoRepository.deleteById(procesoId);
    }
}
