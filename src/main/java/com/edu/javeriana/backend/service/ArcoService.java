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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArcoService implements IArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Arco crearArco(ArcoRegistroDTO dto) {

        // 1. Validar que el proceso exista
        Proceso proceso = procesoRepository.findById(dto.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado"));

        // 2. Validar que el usuario tenga permisos (autor o admin)
        validarUsuarioAutorizado(proceso, dto.getUsuarioId());

        // 3. Parsear los tipos de nodo
        TipoNodo origenTipo = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        // 3. Validar que origen y destino no sean el mismo nodo
        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        // 4. Validar que no exista un arco duplicado entre los mismos nodos
        boolean existeArco = arcoRepository.existsByProcesoIdAndOrigenIdAndOrigenTipoAndDestinoIdAndDestinoTipo(
                dto.getProcesoId(), dto.getOrigenId(), origenTipo, dto.getDestinoId(), destinoTipo);

        if (existeArco) {
            throw new BusinessRuleException(
                    "Ya existe un arco entre estos dos nodos en el proceso");
        }

        // 5. Crear y guardar el arco
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

        // 1. Buscar el arco existente
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));

        // 2. Validar que el usuario tenga permisos (autor o admin)
        validarUsuarioAutorizado(arco.getProceso(), dto.getUsuarioId());

        // 3. Parsear los nuevos tipos de nodo
        TipoNodo origenTipo = parseTipoNodo(dto.getOrigenTipo());
        TipoNodo destinoTipo = parseTipoNodo(dto.getDestinoTipo());

        // 3. Validar que origen y destino no sean el mismo nodo
        if (dto.getOrigenId().equals(dto.getDestinoId()) && origenTipo == destinoTipo) {
            throw new BusinessRuleException("El nodo de origen y destino no pueden ser el mismo");
        }

        // 4. Validar que no exista otro arco duplicado (excluyendo el actual)
        //    Solo verificar si el origen/destino realmente cambió
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

        // 5. Actualizar los campos
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
        if (!procesoRepository.existsById(procesoId)) {
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
        // Validar que el usuario sea administrador
        validarUsuarioAdministrador(usuarioId);

        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arco no encontrado"));
        arcoRepository.delete(arco);
    }

    @Override
    @Transactional
    public void eliminarArcosPorProceso(Long procesoId, Long usuarioId) {
        // Validar que el usuario sea administrador
        validarUsuarioAdministrador(usuarioId);

        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado");
        }
        arcoRepository.deleteByProcesoId(procesoId);
    }

    /**
     * Valida que el usuario exista y tenga rol ADMINISTRADOR_EMPRESA.
     */
    private void validarUsuarioAdministrador(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"ADMINISTRADOR_EMPRESA".equals(usuario.getRol())) {
            throw new BusinessRuleException(
                    "Solo un administrador puede eliminar arcos");
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
                    "No tienes permisos para modificar los arcos de este proceso. Solo el autor o un administrador pueden hacerlo.");
        }
    }

    /**
     * Convierte el string del DTO al enum TipoNodo.
     */
    private TipoNodo parseTipoNodo(String tipo) {
        try {
            return TipoNodo.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Tipo de nodo no válido: '" + tipo + "'. Los valores permitidos son: ACTIVIDAD, GATEWAY");
        }
    }
}
