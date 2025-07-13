package com.backend.insumo_servicio.service;

import com.backend.insumo_servicio.dto.ServicioRequestDTO;
import com.backend.insumo_servicio.dto.ServicioResponseDTO;
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.model.Insumo;
import com.backend.insumo_servicio.model.Servicio;
import com.backend.insumo_servicio.model.TipoInsumo; // Necesaria para Insumo
import com.backend.insumo_servicio.repository.InsumoRepository;
import com.backend.insumo_servicio.repository.ServicioRepository;
import com.backend.insumo_servicio.service.impl.ServicioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicioServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private InsumoRepository insumoRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    private Servicio servicioActivo;
    private Servicio servicioInactivo;
    private Insumo insumoEjemplo1;
    private Insumo insumoEjemplo2;
    private TipoInsumo tipoInsumoEjemplo;

    @BeforeEach
    void setUp() {
        tipoInsumoEjemplo = new TipoInsumo(10L, "Medicamento", true);
        insumoEjemplo1 = new Insumo(100L, "Paracetamol", 50, new Date(), 10, true, tipoInsumoEjemplo);
        insumoEjemplo2 = new Insumo(101L, "Vendas", 20, new Date(), 5, true, tipoInsumoEjemplo);

        servicioActivo = new Servicio(1L, "Consulta General", "Diagnóstico y tratamiento básico", 30000, true);
        servicioActivo.setInsumos(Arrays.asList(insumoEjemplo1)); // Asignamos insumos para las pruebas
        
        servicioInactivo = new Servicio(2L, "Vacunación", "Aplicación de vacunas", 25000, false);
        servicioInactivo.setInsumos(Arrays.asList(insumoEjemplo2));
    }

    @Test
    @DisplayName("Obtener Servicios Activos - Debería retornar solo los servicios con estado true")
    void testObtenerServiciosActivos() {
        // Arrange
        List<Servicio> allServicios = Arrays.asList(servicioActivo, servicioInactivo);
        when(servicioRepository.findAll()).thenReturn(allServicios);

        // Act
        List<ServicioResponseDTO> result = servicioService.obtenerServiciosActivos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(servicioActivo.getNombreServicio(), result.get(0).getNombreServicio());
        assertTrue(result.get(0).getEstado());
        assertFalse(result.get(0).getInsumos().isEmpty()); // Verifica que los insumos también se mapean
        assertEquals(1, result.get(0).getInsumos().size());
        assertEquals(insumoEjemplo1.getNombre(), result.get(0).getInsumos().get(0).getNombre());

        verify(servicioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar Servicio por ID - Debería retornar el DTO si el servicio existe")
    void testBuscarServicioPorId_Existente() {
        // Arrange
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioActivo));

        // Act
        ServicioResponseDTO result = servicioService.buscarServicioPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(servicioActivo.getIdServicio(), result.getId());
        assertEquals(servicioActivo.getNombreServicio(), result.getNombreServicio());
        assertFalse(result.getInsumos().isEmpty());
        verify(servicioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Buscar Servicio por ID - Debería lanzar ResourceNotFoundException si el servicio no existe")
    void testBuscarServicioPorId_NoExistente() {
        // Arrange
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> servicioService.buscarServicioPorId(99L));
        verify(servicioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Crear Servicio - Debería guardar un nuevo servicio con sus insumos asociados")
    void testCrearServicio_Exito() {
        // Arrange
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Cirugía Menor");
        requestDTO.setDescripcionServicio("Procedimiento quirúrgico básico");
        requestDTO.setPrecio(50000);
        requestDTO.setEstado(true);
        requestDTO.setInsumoIds(Arrays.asList(100L, 101L)); // IDs de insumos existentes

        when(insumoRepository.findById(100L)).thenReturn(Optional.of(insumoEjemplo1));
        when(insumoRepository.findById(101L)).thenReturn(Optional.of(insumoEjemplo2));

        Servicio servicioAGuardar = new Servicio(); // Simula la instancia que se crearía
        servicioAGuardar.setNombreServicio(requestDTO.getNombreServicio());
        servicioAGuardar.setDescripcionServicio(requestDTO.getDescripcionServicio());
        servicioAGuardar.setPrecio(requestDTO.getPrecio());
        servicioAGuardar.setEstado(requestDTO.getEstado());
        servicioAGuardar.setInsumos(Arrays.asList(insumoEjemplo1, insumoEjemplo2));

        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> {
            Servicio saved = invocation.getArgument(0);
            saved.setIdServicio(3L); // Simula el ID asignado por la DB
            return saved;
        });

        // Act
        ServicioResponseDTO result = servicioService.crearServicio(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Cirugía Menor", result.getNombreServicio());
        assertTrue(result.getEstado());
        assertEquals(2, result.getInsumos().size());
        assertTrue(result.getInsumos().stream().anyMatch(i -> i.getNombre().equals("Paracetamol")));
        assertTrue(result.getInsumos().stream().anyMatch(i -> i.getNombre().equals("Vendas")));

        verify(insumoRepository, times(1)).findById(100L);
        verify(insumoRepository, times(1)).findById(101L);
        verify(servicioRepository, times(1)).save(any(Servicio.class));
    }

    @Test
    @DisplayName("Crear Servicio - Debería lanzar ResourceNotFoundException si un insumo no es encontrado")
    void testCrearServicio_InsumoNoEncontrado() {
        // Arrange
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Servicio Fallido");
        requestDTO.setInsumoIds(Arrays.asList(100L, 999L)); // 999L no existe

        when(insumoRepository.findById(100L)).thenReturn(Optional.of(insumoEjemplo1));
        when(insumoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () ->
            servicioService.crearServicio(requestDTO)
        );
        assertEquals("Insumo con ID 999 no encontrado", thrown.getMessage());
        verify(insumoRepository, times(1)).findById(100L); // Se llama para el primer insumo
        verify(insumoRepository, times(1)).findById(999L); // Se llama para el insumo faltante
        verify(servicioRepository, never()).save(any(Servicio.class)); // No debería guardar
    }

    @Test
    @DisplayName("Actualizar Servicio - Debería modificar un servicio existente y sus insumos")
    void testActualizarServicio_Exito() {
        // Arrange
        Long servicioId = 1L;
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Consulta Avanzada");
        requestDTO.setDescripcionServicio("Diagnóstico y tratamiento avanzado");
        requestDTO.setPrecio(45000);
        requestDTO.setEstado(true);
        requestDTO.setInsumoIds(Arrays.asList(101L)); // Cambiar a solo insumoEjemplo2

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicioActivo));
        when(insumoRepository.findById(101L)).thenReturn(Optional.of(insumoEjemplo2));
        
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> {
            Servicio updated = invocation.getArgument(0);
            updated.setIdServicio(servicioId); // Asegura que el ID se mantiene
            return updated;
        });

        // Act
        ServicioResponseDTO result = servicioService.actualizarServicio(servicioId, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(servicioId, result.getId());
        assertEquals("Consulta Avanzada", result.getNombreServicio());
        assertEquals(45000, result.getPrecio());
        assertTrue(result.getEstado());
        assertEquals(1, result.getInsumos().size());
        assertEquals("Vendas", result.getInsumos().get(0).getNombre());

        verify(servicioRepository, times(1)).findById(servicioId);
        verify(insumoRepository, times(1)).findById(101L);
        verify(servicioRepository, times(1)).save(any(Servicio.class));
    }

    @Test
    @DisplayName("Actualizar Servicio - Debería lanzar ResourceNotFoundException si el servicio a actualizar no existe")
    void testActualizarServicio_ServicioNoEncontrado() {
        // Arrange
        Long servicioId = 99L;
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        when(servicioRepository.findById(servicioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> servicioService.actualizarServicio(servicioId, requestDTO));
        verify(servicioRepository, times(1)).findById(servicioId);
        verify(insumoRepository, never()).findById(anyLong()); // No debería buscar insumos
        verify(servicioRepository, never()).save(any(Servicio.class)); // No debería guardar
    }

    @Test
    @DisplayName("Actualizar Servicio - Debería lanzar ResourceNotFoundException si un insumo para actualizar no es encontrado")
    void testActualizarServicio_InsumoNoEncontrado() {
        // Arrange
        Long servicioId = 1L;
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setInsumoIds(Arrays.asList(999L)); // Insumo que no existe

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicioActivo));
        when(insumoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () ->
            servicioService.actualizarServicio(servicioId, requestDTO)
        );
        assertEquals("Insumo con ID 999 no encontrado", thrown.getMessage());
        verify(servicioRepository, times(1)).findById(servicioId);
        verify(insumoRepository, times(1)).findById(999L);
        verify(servicioRepository, never()).save(any(Servicio.class));
    }

    @Test
    @DisplayName("Eliminar Servicio - Debería cambiar el estado del servicio a false (eliminación lógica)")
    void testEliminarServicio_Exito() {
        // Arrange
        Long servicioId = 1L;
        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicioActivo));

        // Act
        servicioService.eliminarServicio(servicioId);

        // Assert
        assertFalse(servicioActivo.getEstado()); // Verifica que el estado se cambió en el objeto mock
        verify(servicioRepository, times(1)).findById(servicioId);
        verify(servicioRepository, times(1)).save(servicioActivo);
    }

    @Test
    @DisplayName("Eliminar Servicio - Debería lanzar ResourceNotFoundException si el servicio no existe")
    void testEliminarServicio_NoEncontrado() {
        // Arrange
        Long servicioId = 99L;
        when(servicioRepository.findById(servicioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> servicioService.eliminarServicio(servicioId));
        verify(servicioRepository, times(1)).findById(servicioId);
        verify(servicioRepository, never()).save(any(Servicio.class));
    }
}
