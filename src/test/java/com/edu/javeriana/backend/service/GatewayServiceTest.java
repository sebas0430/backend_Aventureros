package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.dto.GatewayEdicionDTO;
import com.edu.javeriana.backend.dto.GatewayRegistroDTO;
import com.edu.javeriana.backend.exception.BusinessRuleException;
import com.edu.javeriana.backend.exception.ResourceNotFoundException;
import com.edu.javeriana.backend.model.Gateway;
import com.edu.javeriana.backend.model.Proceso;
import com.edu.javeriana.backend.model.TipoGateway;
import com.edu.javeriana.backend.model.Usuario;
import com.edu.javeriana.backend.repository.GatewayRepository;
import com.edu.javeriana.backend.service.interfaces.IArcoService;
import com.edu.javeriana.backend.service.interfaces.IProcesoService;
import com.edu.javeriana.backend.service.interfaces.IUsuarioService;
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
class GatewayServiceTest {

    @Mock
    private GatewayRepository gatewayRepository;

    @Mock
    private IProcesoService procesoService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IArcoService arcoService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GatewayService gatewayService;

    private Proceso proceso;
    private Usuario adminUsuario;
    private Gateway gateway;

    @BeforeEach
    void setUp() {
        proceso = new Proceso();
        proceso.setId(1L);
        Usuario autor = new Usuario();
        autor.setId(1L);
        proceso.setAutor(autor);

        adminUsuario = new Usuario();
        adminUsuario.setId(1L);
        adminUsuario.setRol("ADMINISTRADOR_EMPRESA");

        gateway = Gateway.builder()
                .id(1L)
                .nombre("Gateway 1")
                .tipo(TipoGateway.EXCLUSIVO)
                .proceso(proceso)
                .build();
    }

    @Test
    void crearGateway_Exitoso() {
        GatewayRegistroDTO dto = new GatewayRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setNombre("Gateway X");
        dto.setTipo("EXCLUSIVO");

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(gatewayRepository.save(any(Gateway.class))).thenReturn(gateway);
        when(modelMapper.map(any(), eq(GatewayRegistroDTO.class))).thenReturn(dto);

        GatewayRegistroDTO res = gatewayService.crearGateway(dto);

        assertNotNull(res);
        verify(gatewayRepository).save(any());
    }

    @Test
    void crearGateway_TipoInvalido() {
        GatewayRegistroDTO dto = new GatewayRegistroDTO();
        dto.setProcesoId(1L);
        dto.setUsuarioId(1L);
        dto.setTipo("INVALID_TYPE");

        when(procesoService.obtenerProcesoEntity(1L)).thenReturn(proceso);
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);

        assertThrows(BusinessRuleException.class, () -> gatewayService.crearGateway(dto));
    }

    @Test
    void editarGateway_Exitoso() {
        GatewayEdicionDTO dto = new GatewayEdicionDTO();
        dto.setUsuarioId(1L);
        dto.setNombre("Gateway Updated");
        dto.setTipo("PARALELO");

        when(gatewayRepository.findById(1L)).thenReturn(Optional.of(gateway));
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(gatewayRepository.save(any())).thenReturn(gateway);
        when(modelMapper.map(any(), eq(GatewayEdicionDTO.class))).thenReturn(dto);

        GatewayEdicionDTO res = gatewayService.editarGateway(1L, dto);
        assertNotNull(res);
        assertEquals(TipoGateway.PARALELO, gateway.getTipo());
    }

    @Test
    void eliminarGateway_Exitoso() {
        when(usuarioService.obtenerUsuarioEntity(1L)).thenReturn(adminUsuario);
        when(gatewayRepository.findById(1L)).thenReturn(Optional.of(gateway));

        gatewayService.eliminarGateway(1L, 1L);
        verify(arcoService).eliminarArcosPorNodo(anyLong(), eq(1L), any());
        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void listarGatewaysPorProceso_Exitoso() {
        when(procesoService.existsProceso(1L)).thenReturn(true);
        when(gatewayRepository.findByProcesoId(1L)).thenReturn(List.of(gateway));
        when(modelMapper.map(any(), eq(GatewayRegistroDTO.class))).thenReturn(new GatewayRegistroDTO());

        List<GatewayRegistroDTO> list = gatewayService.listarGatewaysPorProceso(1L);
        assertFalse(list.isEmpty());
    }
}
