package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Gateway;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoGateway;
import com.edu.javeriana.backend.model.Usuario;
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
    @Transactional(readOnly = true)
    public List<Gateway> listarGatewaysPorProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return gatewayRepository.findByProcesoId(procesoId);
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
