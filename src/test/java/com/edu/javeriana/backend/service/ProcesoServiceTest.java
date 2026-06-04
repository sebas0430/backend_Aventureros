package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.ProcesoCompartirDTO;
import com.edu.javeriana.backend.dto.ProcesoEdicionDTO;
import com.edu.javeriana.backend.dto.ProcesoRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;

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
        when(procesoRepository.findByEmpresaIdActive(1L)).thenReturn(List.of(proceso));
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

    @Test
    void listarPorAutor_Exitoso() {
        when(procesoRepository.findByAutorIdActive(1L)).thenReturn(List.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.listarPorAutor(1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void filtrarProcesos_SinFiltros_Exitoso() {
        when(procesoRepository.buscarConFiltros(1L, null, null)).thenReturn(List.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.filtrarProcesos(1L, null, null);
        assertFalse(list.isEmpty());
    }

    @Test
    void filtrarProcesos_EstadoInvalido_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> procesoService.filtrarProcesos(1L, "ESTADO_INEXISTENTE", null));
    }

    @Test
    void actualizarDefinicion_Exitoso() {
        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(any())).thenReturn(proceso);
        when(modelMapper.map(any(), eq(ProcesoEdicionDTO.class))).thenReturn(new ProcesoEdicionDTO());

        ProcesoEdicionDTO res = procesoService.actualizarDefinicion(1L, "{\"nodes\":[]}");
        assertNotNull(res);
        assertEquals("{\"nodes\":[]}", proceso.getDefinicionJson());
    }

    @Test
    void compartirProceso_Exitoso() {
        ProcesoCompartirDTO dto = new ProcesoCompartirDTO();
        dto.setUsuarioId(1L);
        dto.setPoolDestinoId(2L);
        dto.setPermiso(PermisoCompartido.LECTURA);

        Pool poolDestino = Pool.builder().id(2L).empresa(empresa).build();

        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(poolRepository.findById(2L)).thenReturn(Optional.of(poolDestino));
        when(procesoCompartidoRepository.findByProcesoIdAndPoolDestinoId(1L, 2L)).thenReturn(Optional.empty());

        procesoService.compartirProceso(1L, dto);

        verify(procesoCompartidoRepository).save(any());
        verify(historialProcesoService).registrarAccion(any(), any(), eq("COMPARTIR"), anyString());
    }

    @Test
    void compartirProceso_UsuarioNoEsAdmin_LanzaExcepcion() {
        Usuario noAdmin = new Usuario();
        noAdmin.setId(2L);
        noAdmin.setRol("EDITOR");
        noAdmin.setEmpresa(empresa);

        ProcesoCompartirDTO dto = new ProcesoCompartirDTO();
        dto.setUsuarioId(2L);

        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(noAdmin));

        assertThrows(BusinessRuleException.class, () -> procesoService.compartirProceso(1L, dto));
    }

    @Test
    void quitarComparticionProceso_Exitoso() {
        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));

        procesoService.quitarComparticionProceso(1L, 2L, 1L);

        verify(procesoCompartidoRepository).deleteByProcesoIdAndPoolDestinoId(1L, 2L);
        verify(historialProcesoService).registrarAccion(any(), any(), eq("QUITAR_COMPARTICION"), anyString());
    }

    @Test
    void listarProcesosCompartidosConPool_Exitoso() {
        ProcesoCompartido compartido = ProcesoCompartido.builder()
                .proceso(proceso).poolDestino(pool).build();

        when(poolRepository.findById(1L)).thenReturn(Optional.of(pool));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(procesoCompartidoRepository.findByPoolDestinoId(1L)).thenReturn(List.of(compartido));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.listarProcesosCompartidosConPool(1L, 1L);
        assertFalse(list.isEmpty());
    }

    @Test
    void listarProcesosPorUsuario_Admin_VeTodo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));
        when(procesoRepository.buscarConFiltros(1L, null, null)).thenReturn(List.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.listarProcesosPorUsuario(1L, 1L, null);
        assertFalse(list.isEmpty());
    }

    @Test
    void eliminarProceso_YaInactivo_LanzaExcepcion() {
        proceso.setEstado(EstadoProceso.INACTIVO);

        when(procesoRepository.findById(1L)).thenReturn(Optional.of(proceso));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUsuario));

        assertThrows(BusinessRuleException.class, () -> procesoService.eliminarProceso(1L, 1L));
    }

    @Test
    void filtrarProcesos_ConEstadoValido_Exitoso() {
        when(procesoRepository.buscarConFiltros(1L, EstadoProceso.BORRADOR, null)).thenReturn(List.of(proceso));
        when(modelMapper.map(any(), eq(ProcesoRegistroDTO.class))).thenReturn(new ProcesoRegistroDTO());

        List<ProcesoRegistroDTO> list = procesoService.filtrarProcesos(1L, "BORRADOR", null);
        assertFalse(list.isEmpty());
    }

    @Test
    void listarProcesosPorUsuario_UsuarioSinPools_RetornaVacio() {
        Usuario usuarioNormal = new Usuario();
        usuarioNormal.setId(2L);
        usuarioNormal.setRol("EDITOR");
        usuarioNormal.setEmpresa(empresa);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioNormal));
        when(asignacionRolPoolRepository.findByUsuarioId(2L)).thenReturn(List.of());

        List<ProcesoRegistroDTO> list = procesoService.listarProcesosPorUsuario(2L, 1L, null);
        assertTrue(list.isEmpty());
    }
}
