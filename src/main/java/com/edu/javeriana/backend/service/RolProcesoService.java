package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IRolProcesoService;
import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Actividad;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.RolProceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.RolProcesoRepository;
import com.edu.javeriana.backend.service.interfaces.IActividadService;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class RolProcesoService implements IRolProcesoService {

    private final RolProcesoRepository rolProcesoRepository;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IActividadService actividadService;
    private final ModelMapper modelMapper;

    public RolProcesoService(RolProcesoRepository rolProcesoRepository,
                             @Lazy IEmpresaService empresaService,
                             @Lazy IUsuarioService usuarioService,
                             @Lazy IActividadService actividadService,
                             ModelMapper modelMapper) {
        this.rolProcesoRepository = rolProcesoRepository;
        this.empresaService       = empresaService;
        this.usuarioService       = usuarioService;
        this.actividadService     = actividadService;
        this.modelMapper          = modelMapper;
    }

    @Override
    @Transactional
    public RolProcesoRegistroDTO crearRolProceso(RolProcesoRegistroDTO dto) {
        Empresa empresa = empresaService.obtenerEmpresaEntity(dto.getEmpresaId());

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(dto.getUsuarioId());

        if (!solicitante.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessRuleException("No perteneces a esta empresa");
        }

        if (!"ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador de la empresa puede crear roles de proceso");
        }

        if (rolProcesoRepository.existsByEmpresaIdAndNombre(empresa.getId(), dto.getNombre())) {
            throw new BusinessRuleException(
                    "Ya existe un rol de proceso con el nombre '" + dto.getNombre() + "' en esta empresa");
        }

        RolProceso rolProceso = RolProceso.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .empresa(empresa)
                .build();

        RolProceso guardado = rolProcesoRepository.save(rolProceso);
        log.info("AUDITORIA: Usuario {} (ADMIN) creó el Rol de Proceso '{}' (ID={}) en la Empresa ID={}",
                solicitante.getId(), guardado.getNombre(), guardado.getId(), empresa.getId());

        RolProcesoRegistroDTO response = modelMapper.map(guardado, RolProcesoRegistroDTO.class);
        response.setEmpresaId(guardado.getEmpresa().getId());
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional
    public RolProcesoEdicionDTO editarRolProceso(Long id, RolProcesoEdicionDTO dto) {
        RolProceso rol = rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de proceso no encontrado"));

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(dto.getUsuarioId());

        if (!solicitante.getEmpresa().getId().equals(rol.getEmpresa().getId())) {
            throw new BusinessRuleException("No perteneces a la empresa de este rol");
        }

        if (!"ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador de la empresa puede editar roles de proceso");
        }

        if (!rol.getNombre().equals(dto.getNombre())
                && rolProcesoRepository.existsByEmpresaIdAndNombre(rol.getEmpresa().getId(), dto.getNombre())) {
            throw new BusinessRuleException(
                    "Ya existe un rol de proceso con el nombre '" + dto.getNombre() + "' en esta empresa");
        }

        StringBuilder cambios = new StringBuilder();
        if (!rol.getNombre().equals(dto.getNombre())) {
            cambios.append("Nombre cambiado de '").append(rol.getNombre())
                    .append("' a '").append(dto.getNombre()).append("'. ");
            rol.setNombre(dto.getNombre());
        }
        String descAnterior = rol.getDescripcion() != null ? rol.getDescripcion() : "";
        String descNueva    = dto.getDescripcion() != null ? dto.getDescripcion() : "";
        if (!descAnterior.equals(descNueva)) {
            cambios.append("Descripción actualizada. ");
            rol.setDescripcion(dto.getDescripcion());
        }

        RolProceso actualizado = rolProcesoRepository.save(rol);
        log.info("AUDITORIA: Usuario {} (ADMIN) editó el Rol de Proceso ID={} — {}",
                solicitante.getId(), actualizado.getId(),
                cambios.isEmpty() ? "Sin cambios detectados" : cambios.toString().trim());

        RolProcesoEdicionDTO response = modelMapper.map(actualizado, RolProcesoEdicionDTO.class);
        response.setUsuarioId(dto.getUsuarioId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolProcesoRegistroDTO> listarRolesPorEmpresa(Long empresaId, Long usuarioId) {
        Empresa empresa = empresaService.obtenerEmpresaEntity(empresaId);

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!solicitante.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessRuleException("No perteneces a esta empresa");
        }

        return rolProcesoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(r -> {
                    RolProcesoRegistroDTO dto = modelMapper.map(r, RolProcesoRegistroDTO.class);
                    dto.setEmpresaId(r.getEmpresa().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void eliminarRolProceso(Long id, Long usuarioId) {
        RolProceso rol = rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de proceso no encontrado"));

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!solicitante.getEmpresa().getId().equals(rol.getEmpresa().getId())) {
            throw new BusinessRuleException("No perteneces a la empresa de este rol");
        }

        if (!"ADMINISTRADOR_EMPRESA".equals(solicitante.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador de la empresa puede eliminar roles de proceso");
        }

        if (actividadService.existePorRolProceso(rol.getId())) {
            throw new BusinessRuleException(
                    "No se puede eliminar el rol '" + rol.getNombre()
                    + "' porque está asignado a una o más actividades. Reasigne las actividades primero.");
        }

        rolProcesoRepository.delete(rol);
        log.info("AUDITORIA: Usuario {} (ADMIN) eliminó el Rol de Proceso '{}' (ID={}) de la Empresa ID={}",
                solicitante.getId(), rol.getNombre(), id, rol.getEmpresa().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RolProcesoRegistroDTO obtenerRolProcesoPorId(Long id) {
        RolProceso rol = rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de proceso no encontrado"));

        RolProcesoRegistroDTO dto = modelMapper.map(rol, RolProcesoRegistroDTO.class);
        dto.setEmpresaId(rol.getEmpresa().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolProcesoDetalleDTO> consultarRolesConDetalle(Long empresaId, Long usuarioId) {
        Empresa empresa = empresaService.obtenerEmpresaEntity(empresaId);

        Usuario solicitante = usuarioService.obtenerUsuarioEntity(usuarioId);

        if (!solicitante.getEmpresa().getId().equals(empresa.getId())) {
            throw new BusinessRuleException("No perteneces a esta empresa");
        }

        return rolProcesoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::construirDetalle)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolProcesoDetalleDTO consultarRolProcesoDetalle(Long id) {
        RolProceso rol = rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de proceso no encontrado"));
        return construirDetalle(rol);
    }

    // ===================== Helpers =====================

    private RolProcesoDetalleDTO construirDetalle(RolProceso rol) {
        List<Actividad> actividades = actividadService.obtenerActividadesPorRolProceso(rol.getId());

        Map<Long, RolProcesoDetalleDTO.ProcesoUsoDTO> procesoMap = new LinkedHashMap<>();

        for (Actividad act : actividades) {
            Long procesoId = act.getProceso().getId();

            procesoMap.computeIfAbsent(procesoId, k ->
                    RolProcesoDetalleDTO.ProcesoUsoDTO.builder()
                            .procesoId(procesoId)
                            .procesoNombre(act.getProceso().getNombre())
                            .actividades(new ArrayList<>())
                            .build()
            );

            procesoMap.get(procesoId).getActividades().add(
                    RolProcesoDetalleDTO.ActividadUsoDTO.builder()
                            .actividadId(act.getId())
                            .actividadNombre(act.getNombre())
                            .tipoActividad(act.getTipoActividad())
                            .build()
            );
        }

        return RolProcesoDetalleDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .empresaId(rol.getEmpresa().getId())
                .usoEnProcesos(new ArrayList<>(procesoMap.values()))
                .build();
    }
}