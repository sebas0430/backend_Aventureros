package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.IArcoService;
import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ArcoRepository;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class ArcoService implements IArcoService {
    private static final String ARCO_NO_ENCONTRADO = "Arco no encontrado";
    private final ArcoRepository arcoRepository;
    private final IProcesoService procesoService;
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public ArcoService(ArcoRepository arcoRepository,
                       @Lazy IProcesoService procesoService,
                       @Lazy IUsuarioService usuarioService,
                       ModelMapper modelMapper) {
        this.arcoRepository   = arcoRepository;
        this.procesoService   = procesoService;
        this.usuarioService   = usuarioService;
        this.modelMapper       = modelMapper;
    }

    @Override
    @Transactional
    public ArcoRegistroDTO crearArco(ArcoRegistroDTO dto) {

        Proceso proceso = procesoService.obtenerProcesoEntity(dto.getProcesoId());

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
        
        log.info("Arco {} creado exitosamente en proceso {}", guardado.getId(), proceso.getId());

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
                .orElseThrow(() -> new ResourceNotFoundException(ARCO_NO_ENCONTRADO));

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
        
        log.info("Arco {} editado exitosamente", guardado.getId());

        // Mapear entidad → DTO existente
        ArcoEdicionDTO response = modelMapper.map(guardado, ArcoEdicionDTO.class);
        response.setOrigenTipo(guardado.getOrigenTipo().name());
        response.setDestinoTipo(guardado.getDestinoTipo().name());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArcoRegistroDTO> listarArcosPorProceso(Long procesoId) {
        if (!procesoService.existeProceso(procesoId)) {
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
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArcoRegistroDTO obtenerArcoPorId(Long id) {
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ARCO_NO_ENCONTRADO));

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
                .orElseThrow(() -> new ResourceNotFoundException(ARCO_NO_ENCONTRADO));
        arcoRepository.delete(arco);
        log.info("Arco {} eliminado exitosamente por el usuario {}", id, usuarioId);
    }

    @Override
    @Transactional
    public void eliminarArcosPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);
        if (!procesoService.existeProceso(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        arcoRepository.deleteByProcesoId(procesoId);
        log.info("Todos los arcos del proceso {} han sido eliminados por el usuario admin {}", procesoId, usuarioId);
    }

    @Override
    @Transactional
    public void eliminarArcosPorNodo(Long procesoId, Long nodoId, TipoNodo tipoNodo) {
        List<Arco> arcosOrigen  = arcoRepository.findByProcesoIdAndOrigenIdAndOrigenTipo(procesoId, nodoId, tipoNodo);
        List<Arco> arcosDestino = arcoRepository.findByProcesoIdAndDestinoIdAndDestinoTipo(procesoId, nodoId, tipoNodo);
        
        arcoRepository.deleteAll(arcosOrigen);
        arcoRepository.deleteAll(arcosDestino);
        
        log.info("Se han eliminado los arcos asociados al nodo {} de tipo {} en el proceso {}", nodoId, tipoNodo, procesoId);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException("Solo un administrador puede eliminar arcos");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioEntity(usuarioId);
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
