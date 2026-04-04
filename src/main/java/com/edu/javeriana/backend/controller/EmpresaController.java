package com.edu.javeriana.backend.controller;

import com.edu.javeriana.backend.dto.EmpresaEdicionDTO;
import com.edu.javeriana.backend.dto.EmpresaRegistroDTO;
import com.edu.javeriana.backend.service.IEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final IEmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaRegistroDTO> registrarEmpresa(@Valid @RequestBody EmpresaRegistroDTO dto) {
        EmpresaRegistroDTO response = empresaService.registrarEmpresa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaEdicionDTO> editarEmpresa(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaEdicionDTO dto) {
        EmpresaEdicionDTO response = empresaService.editarEmpresa(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaRegistroDTO> obtenerEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.obtenerEmpresa(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaRegistroDTO>> listarEmpresas() {
        return ResponseEntity.ok(empresaService.listarEmpresas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarEmpresa(@PathVariable Long id) {
        empresaService.eliminarEmpresa(id);
        return ResponseEntity.ok(Map.of("mensaje", "Empresa eliminada exitosamente"));
    }
}
