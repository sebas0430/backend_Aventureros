package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.RolProceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.EmpresaRepository;
import com.edu.javeriana.backend.repository.RolProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolProcesoService implements IRolProcesoService {

    private final RolProcesoRepository rolProcesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public RolProceso crearRolProceso(RolProcesoRegistroDTO dto) {

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Usuario solicitante = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar multitenancy: el usuario debe pertenecer a la empresa
        if (!solicitante.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessRuleException("No perteneces a esta empresa");
        }

        // Solo un administrador de la empresa puede crear roles de proceso
        if (!"ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador de la empresa puede crear roles de proceso");
        }

        // Validar que no exista un rol con el mismo nombre en la misma empresa
        if (rolProcesoRepository.existsByEmpresaIdAndNombre(empresa.getId(), dto.getNombre())) {
            throw new BusinessRuleException(
                    "Ya existe un rol de proceso con el nombre '" + dto.getNombre() + "' en esta empresa");
        }

        RolProceso rolProceso = RolProceso.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .empresa(empresa)
                .build();

        RolProceso rolGuardado = rolProcesoRepository.save(rolProceso);

        log.info("AUDITORIA: Usuario {} (ADMIN) creó el Rol de Proceso '{}' (ID={}) en la Empresa ID={}",
                solicitante.getId(), rolGuardado.getNombre(), rolGuardado.getId(), empresa.getId());

        return rolGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolProceso> listarRolesPorEmpresa(Long empresaId, Long usuarioId) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar multitenancy: el usuario debe pertenecer a la empresa
        if (!solicitante.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessRuleException("No perteneces a esta empresa");
        }

        return rolProcesoRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public RolProceso obtenerRolProcesoPorId(Long id) {
        return rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de proceso no encontrado"));
    }
}
