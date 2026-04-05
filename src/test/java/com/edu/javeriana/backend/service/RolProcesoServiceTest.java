package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.RolProcesoDetalleDTO;
import com.edu.javeriana.backend.dto.RolProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.RolProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Empresa;
import com.edu.javeriana.backend.model.RolProceso;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.RolProcesoRepository;
import com.edu.javeriana.backend.service.interfaces.IActividadService;
import com.edu.javeriana.backend.service.interfaces.IEmpresaService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolProcesoServiceTest {

    @Mock
    private RolProcesoRepository rolProcesoRepository;
    @Mock
    private IEmpresaService empresaService;
    @Mock
    private IUsuarioService usuarioService;
    @Mock
    private IActividadService actividadService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RolProcesoService rolProcesoService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private RolProceso rol;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        rol = RolProceso.builder()
                .id(1L)
                .nombre("Rol P1")
                .empresa(empresa)
                .build();
    }

    @Test
    void crearRolProceso_Exitoso() {
        RolProcesoRegistroDTO dto = new RolProcesoRegistroDTO();
        dto.setEmpresaId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Nuevo Rol P");

        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolProcesoRepository.existsByEmpresaIdAndNombre(1L, "Nuevo Rol P")).thenReturn(false);
        when(rolProcesoRepository.save(any(RolProceso.class))).thenReturn(rol);
        when(modelMapper.map(any(), eq(RolProcesoRegistroDTO.class))).thenReturn(dto);

        RolProcesoRegistroDTO res = rolProcesoService.crearRolProceso(dto);

        assertNotNull(res);
        verify(rolProcesoRepository).save(any());
    }

    @Test
    void editarRolProceso_Exitoso() {
        RolProcesoEdicionDTO dto = new RolProcesoEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Nombre Editado");

        when(rolProcesoRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolProcesoRepository.save(any())).thenReturn(rol);
        when(modelMapper.map(any(), eq(RolProcesoEdicionDTO.class))).thenReturn(dto);

        RolProcesoEdicionDTO res = rolProcesoService.editarRolProceso(1L, dto);
        assertNotNull(res);
        assertEquals("Nombre Editado", rol.getNombre());
    }

    @Test
    void eliminarRolProceso_Exitoso() {
        when(rolProcesoRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(actividadService.existePorRolProceso(1L)).thenReturn(false);

        rolProcesoService.eliminarRolProceso(1L, 1L);
        verify(rolProcesoRepository).delete(rol);
    }

    @Test
    void eliminarRolProceso_ErrorActividadesAsignadas() {
        when(rolProcesoRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(actividadService.existePorRolProceso(1L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> rolProcesoService.eliminarRolProceso(1L, 1L));
    }

    @Test
    void listarRolesPorEmpresa() {
        when(empresaService.obtenerEmpresaEntity(1L)).thenReturn(empresa);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(rolProcesoRepository.findByEmpresaId(1L)).thenReturn(List.of(rol));
        when(modelMapper.map(any(), eq(RolProcesoRegistroDTO.class))).thenReturn(new RolProcesoRegistroDTO());

        List<RolProcesoRegistroDTO> list = rolProcesoService.listarRolesPorEmpresa(1L, 1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void consultarRolProcesoDetalle() {
        when(rolProcesoRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(actividadService.obtenerActividadesPorRolProceso(1L)).thenReturn(Collections.emptyList());

        RolProcesoDetalleDTO res = rolProcesoService.consultarRolProcesoDetalle(1L);
        assertNotNull(res);
        assertTrue(res.getUsoEnProcesos().isEmpty());
    }
}
