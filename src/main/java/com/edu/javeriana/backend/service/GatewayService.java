package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IGatewayService;
import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.GatewayRepository;
import com.edu.javeriana.backend.service.interfaces.IArcoService;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


import java.util.List;



@Slf4j
@Service
public class GatewayService implements IGatewayService {

    private final GatewayRepository gatewayRepository;
    private final IProcesoService procesoService;
    private final IUsuarioService usuarioService;
    private final IArcoService arcoService;
    private final ModelMapper modelMapper;

    public GatewayService(GatewayRepository gatewayRepository,
                          @Lazy IProcesoService procesoService,
                          @Lazy IUsuarioService usuarioService,
                          @Lazy IArcoService arcoService,
                          ModelMapper modelMapper) {
        this.gatewayRepository = gatewayRepository;
        this.procesoService    = procesoService;
        this.usuarioService    = usuarioService;
        this.arcoService       = arcoService;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public GatewayRegistroDTO crearGateway(GatewayRegistroDTO dto) {

        // Buscamos el proceso donde el usuario quiere poner el rombo de decisión.
        Proceso proceso = procesoService.obtenerProcesoEntity(dto.getProcesoId());

        // Validamos que el usuario tenga permiso de mover las piezas del diagrama.
        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        // Convertimos el tipo de gateway (ej. "EXCLUSIVO") para que la base lo entienda.
        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());

        // Creamos el Gateway (rombo) en el sistema.
        Gateway gateway = Gateway.builder()
                .nombre(dto.getNombre())
                .tipo(tipoGateway)
                .proceso(proceso)
                .build();

        // Guardamos y avisamos que todo salió bien.
        Gateway guardado = gatewayRepository.save(gateway);

        // Mapeamos a DTO para devolver la info al frente.
        GatewayRegistroDTO response = modelMapper.map(guardado, GatewayRegistroDTO.class);
        response.setTipo(guardado.getTipo().name());
        response.setProcesoId(guardado.getProceso().getId());
        return response;
    }

    @Override
    @Transactional
    public GatewayEdicionDTO editarGateway(Long id, GatewayEdicionDTO dto) {

        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        validarUsuarioAutorizado(gateway.getProceso(), dto.getUsuarioId());

        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());
        gateway.setNombre(dto.getNombre());
        gateway.setTipo(tipoGateway);

        Gateway guardado = gatewayRepository.save(gateway);

        // Mapear entidad → DTO existente
        GatewayEdicionDTO response = modelMapper.map(guardado, GatewayEdicionDTO.class);
        response.setTipo(guardado.getTipo().name());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayRegistroDTO> listarGatewaysPorProceso(Long procesoId) {
        if (!procesoService.existeProceso(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return gatewayRepository.findByProcesoId(procesoId)
                .stream()
                .map(g -> {
                    GatewayRegistroDTO dto = modelMapper.map(g, GatewayRegistroDTO.class);
                    dto.setTipo(g.getTipo().name());
                    dto.setProcesoId(g.getProceso().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void eliminarGateway(Long id, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        Long procesoId = gateway.getProceso().getId();
        arcoService.eliminarArcosPorNodo(procesoId, id, TipoNodo.GATEWAY);

        gatewayRepository.delete(gateway);
    }

    @Override
    @Transactional
    public void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        if (!procesoService.existeProceso(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        gatewayRepository.deleteByProcesoId(procesoId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede eliminar gateways");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
        boolean esAutor = proceso.getAutor().getId().equals(usuario.getId());
        boolean esAdmin = "ADMINISTRADOR_EMPRESA".equals(usuario.getRol());
        if (!esAutor && !esAdmin) {
            throw new BusinessRuleException(
                    "No tienes permisos para modificar este proceso. Solo el autor o un administrador pueden hacerlo.");
        }
    }

    private TipoGateway parseTipoGateway(String tipo) {
        try {
            return TipoGateway.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Tipo de gateway no válido: '" + tipo + "'. Los valores permitidos son: EXCLUSIVO, PARALELO, INCLUSIVO");
        }
    }
}
