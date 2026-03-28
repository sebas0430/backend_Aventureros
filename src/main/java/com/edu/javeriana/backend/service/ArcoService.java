package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import com.edu.javeriana.backend.dto.ArcoEdicionDTO;
import com.edu.javeriana.backend.dto.ArcoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Arco;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoNodo;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.ArcoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArcoService implements IArcoService {

    private final ArcoRepository arcoRepository;
    private final @Lazy IProcesoService procesoService;
    private final @Lazy IUsuarioService usuarioService;

    @Override
    @Transactional
    public Arco crearArco(ArcoRegistroDTO dto) {

        Proceso proceso = procesoService.obtenerProcesoPorId(dto.getProcesoId());

        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        TipoNodo origenTipo = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        boolean existeArco = arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
                dto.getProcesoId(), dto.getOrigenId(), origenTipo, dto.getDestinoId(), destinoTipo);

        if (existeArco) {
            throw new BusinessRuleException(
                    "Ya existe un arco entre estos dos nodos en el proceso");
        }

        Arco arco = Arco.builder()
                .proceso(proceso)
                .origenId(dto.getOrigenId())
                .origenTipo(origenTipo)
                .destinoId(dto.getDestinoId())
                .destinoTipo(destinoTipo)
                .etiqueta(dto.getEtiqueta())
                .build();

        return arcoRepository.save(arco);
    }

    @Override
    @Transactional
    public Arco editarArco(Long id, ArcoEdicionDTO dto) {

        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));

        validarUsuarioAutorizado(arco.getProceso(), dto.getUsuarioId());

        TipoNodo origenTipo = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        boolean origenCambio = !arco.getOrigenId().equals(dto.getOrigenId()) || arco.getOrigenTipo() != origenTipo;
        boolean destinoCambio = !arco.getDestinoId().equals(dto.getDestinoId()) || arco.getDestinoTipo() != destinoTipo;

        if (origenCambio || destinoCambio) {
            Long procesoId = arco.getProceso().getId();
            boolean existeArco = arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
                    procesoId, dto.getOrigenId(), origenTipo, dto.getDestinoId(), destinoTipo);

            if (existeArco) {
                throw new BusinessRuleException(
                        "Ya existe otro arco entre estos dos nodos en el proceso");
            }
        }

        arco.setOrigenId(dto.getOrigenId());
        arco.setOrigenTipo(origenTipo);
        arco.setDestinoId(dto.getDestinoId());
        arco.setDestinoTipo(destinoTipo);
        arco.setEtiqueta(dto.getEtiqueta());

        return arcoRepository.save(arco);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Arco> listarArcosPorProceso(Long procesoId) {
        if (!procesoService.existeProcesoPorId(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        return arcoRepository.findByProcesoId(procesoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Arco obtenerArcoPorId(Long id) {
        return arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));
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
    public void eliminarArcosPorNodo(Long procesoId, Long nodoId, TipoNodo tipoNodo) {
        List<Arco> arcosOrigen = arcoRepository.findByProcesoIdAndOrigenIdAndOrigenTipo(procesoId, nodoId, tipoNodo);
        List<Arco> arcosDestino = arcoRepository.findByProcesoIdAndDestinoIdAndDestinoTipo(procesoId, nodoId, tipoNodo);

        arcoRepository.deleteAll(arcosOrigen);
        arcoRepository.deleteAll(arcosDestino);
    }

    @Override
    @Transactional
    public void eliminarArcosPorProceso(Long procesoId, Long usuarioId) {
        validarUsuarioAdministrador(usuarioId);

        if (!procesoService.existeProcesoPorId(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        arcoRepository.deleteByProcesoId(procesoId);
    }

    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador puede eliminar arcos");
        }
    }

    private void validarUsuarioAutorizado(Proceso proceso, Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

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
