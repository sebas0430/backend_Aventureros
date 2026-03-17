package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Gateway;
import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoGateway;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ArcoRepository;
import com.edu.javeriana.backend.repository.GatewayRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayService implements IGatewayService {

    private final GatewayRepository gatewayRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArcoRepository arcoRepository;

    @Override
    @Transactional
    public Gateway crearGateway(GatewayRegistroDTO dto) {
        
        // 1. Validar proceso
        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        // 2. Validar que el usuario tenga permisos (autor o admin)
        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        // 3. Parsear el tipo de gateway
        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());

        // 4. Crear
        Gateway gateway = Gateway.builder()
                .nombre(dto.getNombre())
                .tipo(tipoGateway)
                .proceso(proceso)
                .build();

        return gatewayRepository.save(gateway);
    }

    @Override
    @Transactional
    public Gateway editarGateway(Long id, GatewayEdicionDTO dto) {
        // 1. Buscar gateway existente
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        // 2. Validar permisos
        validarUsuarioAutorizado(gateway.getProceso(), dto.getUsuarioId());

        // 3. Modificar campos
        TipoGateway tipoGateway = parseTipoGateway(dto.getTipo());
        gateway.setNombre(dto.getNombre());
        gateway.setTipo(tipoGateway);

        return gatewayRepository.save(gateway);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gateway> listarGatewaysPorProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return gatewayRepository.findByProcesoId(procesoId);
    }

    @Override
    @Transactional
    public void eliminarGateway(Long id, Long usuarioId) {
        // Validar que el usuario sea administrador
        validarUsuarioAdministrador(usuarioId);

        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway no encontrado"));

        // Mantener el flujo consistente eliminando arcos asociados a este gateway
        Long procesoId = gateway.getProceso().getId();
        List<Arco> arcosOrigen = arcoRepository.findByProcesoIdAndOrigenIdAndOrigenTipo(procesoId, id, TipoNodo.GATEWAY);
        List<Arco> arcosDestino = arcoRepository.findByProcesoIdAndDestinoIdAndDestinoTipo(procesoId, id, TipoNodo.GATEWAY);

        arcoRepository.deleteAll(arcosOrigen);
        arcoRepository.deleteAll(arcosDestino);

        gatewayRepository.delete(gateway);
    }

    @Override
    @Transactional
    public void eliminarGatewaysPorProceso(Long procesoId, Long usuarioId) {
        // Validar que el usuario sea administrador
        validarUsuarioAdministrador(usuarioId);

        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        gatewayRepository.deleteByProcesoId(procesoId);
    }

    /**
     * Valida que el usuario exista y tenga rol ADMINISTRADOR_EMPRESA.
     */
    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador puede eliminar gateways");
        }
    }

    /**
     * Valida que el usuario sea el autor del proceso o un administrador.
     */
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

    /**
     * Parsear string a TipoGateway
     */
    private TipoGateway parseTipoGateway(String tipo) {
        try {
            return TipoGateway.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Tipo de gateway no válido: '" + tipo + "'. Los valores permitidos son: EXCLUSIVO, PARALELO, INCLUSIVO");
        }
    }
}
