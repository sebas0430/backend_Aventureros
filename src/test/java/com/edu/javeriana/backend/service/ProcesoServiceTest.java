package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.*;
import com.edu.javeriana.backend.repository.*;
import com.edu.javeriana.backend.service.interfaces.IHistorialProcesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesoServiceTest {

    @Mock
    private ProcesoRepository procesoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PoolRepository poolRepository;
    @Mock
    private ProcesoCompartidoRepository procesoCompartidoRepository;
    @Mock
    private AsignacionRolPoolRepository asignacionRolPoolRepository;
    @Mock
    private IHistorialProcesoService historialProcesoService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProcesoService procesoService;

    private Empresa empresa;
    private Usuario adminUsuario;
    private Pool pool;
    private Proceso proceso;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");
        adminUsuario.setEmpresa(empresa);

        pool = Pool.builder().id(1L).empresa(empresa).build();

        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setNombre("Proceso 1");
        proceso.setEmpresa(empresa);
        proceso.setAutor(adminUsuario);
        proceso.setPool(pool);
        proceso.setEstado(EstadoProceso.BORRADOR);
    }

    @Test
    void crearProceso_Exitoso() {
        ProcesoRegistroDTO dto = new ProcesoRegistroDTO();
        dto.setEmpresaId(1L);
        dto.setAutorId(1L);
        dto.setNombre("Nuevo Proceso");
        dto.setPoolId(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(poolRepository.findById(1L)).thenReturn(Optional.of(pool));
        when(procesoRepository.save(any(Proceso.class))).thenReturn(proceso);
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(dto);

        ProcesoRegistroDTO res = procesoService.crearProceso(dto);

        assertNotNull(res);
        verify(procesoRepository).save(any());
    }

    @Test
    void editarProceso_Exitoso() {
        ProcesoEdicionDTO dto = new ProcesoEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Nombre Editado");
        dto.setDescripcion("Desc");
        dto.setCategoria("Cat");

        proceso.setDescripcion("");
        proceso.setCategoria("");

        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(procesoRepository.save(any())).thenReturn(proceso);
        when(modelMapper.map(any(), eq(ProcesoEdicionDTO.class))).thenReturn(dto);

        ProcesoEdicionDTO res = procesoService.editarProceso(1L, dto);

        assertNotNull(res);
        assertEquals("Nombre Editado", proceso.getNombre());
        verify(historialProcesoService).registrarAccion(any(), any(), eq("EDICION"), anyString());
    }

    @Test
    void editarProceso_FalloPermisos() {
        Usuario normalUser = new Usuario();
        normalUser.setId(2L);
        normalUser.setRol("OBSERVADOR");

        ProcesoEdicionDTO dto = new ProcesoEdicionDTO();
        dto.setUsuarioId(2L);

        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        // Mock permission check for non-admin
        when(asignacionRolPoolRepository.findByUsuarioIdAndPoolId(2L, 1L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> procesoService.editarProceso(1L, dto));
    }

    @Test
    void eliminarProceso_Exitoso() {
        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(procesoRepository.save(any())).thenReturn(proceso);

        procesoService.eliminarProceso(1L, 1L);

        assertEquals(EstadoProceso.INACTIVO, proceso.getEstado());
        verify(historialProcesoService).registrarAccion(any(), any(), eq("ELIMINACION"), anyString());
    }

    @Test
    void cambiarEstado_Exitoso() {
        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(procesoRepository.save(any())).thenReturn(proceso);
        when(modelMapper.map(any(), eq(ProcesoEdicionDTO.class))).thenReturn(new ProcesoEdicionDTO());

        ProcesoEdicionDTO res = procesoService.cambiarEstado(1L, EstadoProceso.PUBLICADO, 1L);
        
        assertNotNull(res);
        assertEquals(EstadoProceso.PUBLICADO, proceso.getEstado());
    }

    @Test
    void listarPorEmpresa() {
        when(procesoRepository.findByEmpresaId(1L)).thenReturn(List.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.listarPorEmpresa(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void obtenerProcesoPorId_Exitoso() {
        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        ProcesoRegistroDTO res = procesoService.obtenerProcesoPorId(1L);
        assertNotNull(res);
    }
}
