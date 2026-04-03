package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.ArcoRepository;
import com.edu.javeriana.backend.repository.GatewayRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GatewayService implements IGatewayService {

    private final GatewayRepository gatewayRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArcoRepository arcoRepository;
    private final ModelMapper modelMapper;

    public GatewayService(GatewayRepository gatewayRepository,
                          ProcesoRepository procesoRepository,
                          UsuarioRepository usuarioRepository,
                          ArcoRepository arcoRepository,
                          ModelMapper modelMapper) {
        this.gatewayRepository = gatewayRepository;
        this.procesoRepository = procesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.arcoRepository    = arcoRepository;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public GatewayRegistroDTO crearGateway(GatewayRegistroDTO dto) {

        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());

        Gateway gateway = Gateway.builder()
                .nombre(dto.getNombre())
                .tipo(tipoGateway)
                .proceso(proceso)
                .build();

        Gateway guardado = gatewayRepository.save(gateway);

        // Mapear entidad → DTO existente
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
        if (!procesoRepository.existsById(procesoId)) {
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
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarGateway(Long id, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        Long procesoId = gateway.getProceso().getId();
        List<Arco> arcosOrigen  = arcoRepository.findByProcesoIdAndOrigenIdAndOrigenTipo(procesoId, id, TipoNodo.GATEWAY);
        List<Arco> arcosDestino = arcoRepository.findByProcesoIdAndDestinoIdAndDestinoTipo(procesoId, id, TipoNodo.GATEWAY);

        arcoRepository.deleteAll(arcosOrigen);
        arcoRepository.deleteAll(arcosDestino);

        gatewayRepository.delete(gateway);
    }

    @Override
    @Transactional
    public void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        gatewayRepository.deleteByProcesoId(procesoId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede eliminar gateways");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
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
