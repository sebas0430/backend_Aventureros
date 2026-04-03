package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ArcoRepository;
import com.edu.javeriana.backend.repository.ProcesoRepository;
import com.edu.javeriana.backend.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArcoService implements IArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public ArcoService(ArcoRepository arcoRepository,
                       ProcesoRepository procesoRepository,
                       UsuarioRepository usuarioRepository,
                       ModelMapper modelMapper) {
        this.arcoRepository   = arcoRepository;
        this.procesoRepository = procesoRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public ArcoRegistroDTO crearArco(ArcoRegistroDTO dto) {

        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        TipoNodo origenTipo  = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        boolean existeArco = arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
                dto.getProcesoId(), dto.getOrigenId(), origenTipo, dto.getDestinoId(), destinoTipo);

        if (existeArco) {
            throw new BusinessRuleException("Ya existe un arco entre estos dos nodos en el proceso");
        }

        Arco arco = Arco.builder()
                .proceso(proceso)
                .origenId(dto.getOrigenId())
                .origenTipo(origenTipo)
                .destinoId(dto.getDestinoId())
                .destinoTipo(destinoTipo)
                .etiqueta(dto.getEtiqueta())
                .build();

        Arco guardado = arcoRepository.save(arco);

        // Mapear entidad → DTO existente
        ArcoRegistroDTO response = modelMapper.map(guardado, ArcoRegistroDTO.class);
        response.setProcesoId(guardado.getProceso().getId());
        response.setOrigenTipo(guardado.getOrigenTipo().name());
        response.setDestinoTipo(guardado.getDestinoTipo().name());
        return response;
    }

    @Override
    @Transactional
    public ArcoEdicionDTO editarArco(Long id, ArcoEdicionDTO dto) {

        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));

        validarUsuarioAutorizado(arco.getProceso(), dto.getUsuarioId());

        TipoNodo origenTipo  = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        boolean origenCambio  = !arco.getOrigenId().equals(dto.getOrigenId()) || arco.getOrigenTipo() != origenTipo;
        boolean destinoCambio = !arco.getDestinoId().equals(dto.getDestinoId()) || arco.getDestinoTipo() != destinoTipo;

        if (origenCambio || destinoCambio) {
            boolean existeArco = arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
                    arco.getProceso().getId(), dto.getOrigenId(), origenTipo, dto.getDestinoId(), destinoTipo);

            if (existeArco) {
                throw new BusinessRuleException("Ya existe otro arco entre estos dos nodos en el proceso");
            }
        }

        arco.setOrigenId(dto.getOrigenId());
        arco.setOrigenTipo(origenTipo);
        arco.setDestinoId(dto.getDestinoId());
        arco.setDestinoTipo(destinoTipo);
        arco.setEtiqueta(dto.getEtiqueta());

        Arco guardado = arcoRepository.save(arco);

        // Mapear entidad → DTO existente
        ArcoEdicionDTO response = modelMapper.map(guardado, ArcoEdicionDTO.class);
        response.setOrigenTipo(guardado.getOrigenTipo().name());
        response.setDestinoTipo(guardado.getDestinoTipo().name());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArcoRegistroDTO> listarArcosPorProceso(Long procesoId) {
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return arcoRepository.findByProcesoId(procesoId)
                .stream()
                .map(a -> {
                    ArcoRegistroDTO dto = modelMapper.map(a, ArcoRegistroDTO.class);
                    dto.setProcesoId(a.getProceso().getId());
                    dto.setOrigenTipo(a.getOrigenTipo().name());
                    dto.setDestinoTipo(a.getDestinoTipo().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArcoRegistroDTO obtenerArcoPorId(Long id) {
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));

        ArcoRegistroDTO response = modelMapper.map(arco, ArcoRegistroDTO.class);
        response.setProcesoId(arco.getProceso().getId());
        response.setOrigenTipo(arco.getOrigenTipo().name());
        response.setDestinoTipo(arco.getDestinoTipo().name());
        return response;
    }

    @Override
    @Transactional
    public void eliminarArco(Long id, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));
        arcoRepository.delete(arco);
    }

    @Override
    @Transactional
    public void eliminarArcosPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        arcoRepository.deleteByProcesoId(procesoId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede eliminar arcos");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean esAutor = proceso.getAutor().getId().equals(usuario.getId());
        boolean esAdmin = "ADMINISTRADOR_EMPRESA".equals(usuario.getRol());
        if (!esAutor && !esAdmin) {
            throw new BusinessRuleException(
                    "No tienes permisos para modificar los arcos de este proceso. Solo el autor o un administrador pueden hacerlo.");
        }
    }

    private TipoNodo parseTipoNodo(String tipo) {
        try {
            return TipoNodo.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Tipo de nodo no válido: '" + tipo + "'. Los valores permitidos son: ACTIVIDAD, GATEWAY");
        }
    }
}
