package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Gateway;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoGateway;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.GatewayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j      
public class GatewayService implements IGatewayService {

    private final GatewayRepository gatewayRepository;
    private final @Lazy IProcesoService procesoService;
    private final @Lazy IUsuarioService usuarioService;
    private final @Lazy IArcoService arcoService;

    @Override
    @Transactional
    public Gateway crearGateway(GatewayRegistroDTO dto) {

        Proceso proceso = procesoService.obtenerProcesoPorId(dto.getProcesoId());

        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());

        Gateway gateway = Gateway.builder()
                .nombre(dto.getNombre())
                .tipo(tipoGateway)
                .proceso(proceso)
                .build();

        log.info("Gateway {} creado exitosamente en proceso {}", gateway.getNombre(), proceso.getId());

        return gatewayRepository.save(gateway);
    }

    @Override
    @Transactional
    public Gateway editarGateway(Long id, GatewayEdicionDTO dto) {
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        validarUsuarioAutorizado(gateway.getProceso(), dto.getUsuarioId());

        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());
        gateway.setNombre(dto.getNombre());
        gateway.setTipo(tipoGateway);

        return gatewayRepository.save(gateway);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gateway> listarGatewaysPorProceso(Long procesoId) {
        if (!procesoService.existeProcesoPorId(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return gatewayRepository.findByProcesoId(procesoId);
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
        log.info("Gateway {} eliminado exitosamente", id);
    }

    @Override
    @Transactional
    public void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        if (!procesoService.existeProcesoPorId(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        gatewayRepository.deleteByProcesoId(procesoId);
    }

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador puede eliminar gateways");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

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
                    "Tipo de gateway no válido: '" + tipo
                            + "'. Los valores permitidos son: EXCLUSIVO, PARALELO, INCLUSIVO");
        }
    }
}
