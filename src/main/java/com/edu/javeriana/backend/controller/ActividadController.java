package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.ActividadEdicionDTO;
import com.edu.javeriana.backend.dto.ActividadRegistroDTO;
import com.edu.javeriana.backend.service.interfaces.IActividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final IActividadService actividadService;

    // Crea una tarea o paso nuevo (se asegura de que todo esté en orden antes de guardar).
    @PostMapping
    public ResponseEntity<ActividadRegistroDTO> crearActividad(
            @Valid @RequestBody ActividadRegistroDTO dto) {
        ActividadRegistroDTO response = actividadService.crearActividad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Cambia los datos de una tarea (como el nombre o quién la hace).
    @PutMapping("/{id}")
    public ResponseEntity<ActividadEdicionDTO> editarActividad(
            @PathVariable Long id,
            @Valid @RequestBody ActividadEdicionDTO dto) {
        ActividadEdicionDTO response = actividadService.editarActividad(id, dto);
        return ResponseEntity.ok(response);
    }

    // Borra una tarea y reacomoda el orden de las que quedan.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarActividad(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        actividadService.eliminarActividad(id, usuarioId);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Actividad eliminada y flujo del proceso reajustado exitosamente"));
    }

    // Trae la lista de todas las tareas que pertenecen a un proceso específico.
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<ActividadRegistroDTO>> listarPorProceso(
            @PathVariable Long procesoId) {
        return ResponseEntity.ok(actividadService.listarPorProceso(procesoId));
    }

    // Busca una tarea solita usando su ID.
    @GetMapping("/{id}")
    public ResponseEntity<ActividadRegistroDTO> obtenerActividad(@PathVariable Long id) {
        return ResponseEntity.ok(actividadService.obtenerPorId(id));
    }
}