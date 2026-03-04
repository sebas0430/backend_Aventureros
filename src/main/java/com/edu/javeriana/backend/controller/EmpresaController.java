package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<?> registrarEmpresa(@Valid @RequestBody EmpresaRegistroDTO dto) {
        try {
            Empresa nuevaEmpresa = empresaService.registrarEmpresa(dto);

            // Retornamos un Response básico, en producción usar un Mapper hacia un DTO de
            // salida
            Map<String, Object> response = new HashMap<>();
            response.put("id", nuevaEmpresa.getId());
            response.put("nombre", nuevaEmpresa.getNombre());
            response.put("mensaje", "Empresa registrada exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
