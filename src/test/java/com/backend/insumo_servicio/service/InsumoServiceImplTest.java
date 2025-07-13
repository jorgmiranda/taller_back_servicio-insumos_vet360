package com.backend.insumo_servicio.service;

import com.backend.insumo_servicio.dto.InsumoRequestDTO;
import com.backend.insumo_servicio.dto.InsumoResponseDTO;
//import com.backend.insumo_servicio.dto.TipoInsumoDTO; // Asume que tienes esta DTO
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.model.Insumo;
import com.backend.insumo_servicio.model.TipoInsumo; // Asume que tienes esta entidad
import com.backend.insumo_servicio.repository.InsumoRepository;
import com.backend.insumo_servicio.repository.TipoInsumoRepository; // Asume que tienes este repositorio
import com.backend.insumo_servicio.service.impl.InsumoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
//import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InsumoServiceImplTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private TipoInsumoRepository tipoInsumoRepository;

    @InjectMocks
    private InsumoServiceImpl insumoService;

    private Insumo insumoActivo;
    private Insumo insumoInactivo;
    private TipoInsumo tipoInsumoEjemplo;

    @BeforeEach
    void setUp() {
        // Inicializa un TipoInsumo de ejemplo
        tipoInsumoEjemplo = new TipoInsumo();
        tipoInsumoEjemplo.setIdTipoInsumo(10L);
        tipoInsumoEjemplo.setNombreTipo("Medicamento");
        tipoInsumoEjemplo.setEstado(true);
        
        // Inicializa un insumo activo
        insumoActivo = new Insumo(1L, "Paracetamol", 50, new Date(), 10, true, tipoInsumoEjemplo);
        
        // Inicializa un insumo inactivo
        insumoInactivo = new Insumo(2L, "Vendas", 20, new Date(), 5, false, tipoInsumoEjemplo);
    }

    @Test
    @DisplayName("Obtener Insumos Activos - Debería retornar solo insumos con estado true")
    void testObtenerInsumosActivos() {
        // Arrange: Configura el comportamiento de los mocks
        List<Insumo> allInsumos = Arrays.asList(insumoActivo, insumoInactivo);
        when(insumoRepository.findAll()).thenReturn(allInsumos);

        // Act: Llama al método del servicio
        List<InsumoResponseDTO> result = insumoService.obtenerInsumosActivos();

        // Assert: Verifica los resultados
        assertNotNull(result);
        assertEquals(1, result.size()); // Solo debe devolver el insumo activo
        assertEquals(insumoActivo.getNombre(), result.get(0).getNombre());
        assertTrue(result.get(0).getEstado());

        // Verify: Asegura que el método del repositorio fue llamado
        verify(insumoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar Insumo por ID - Debería retornar el insumo si existe")
    void testBuscarInsumoPorId_Existente() {
        // Arrange
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumoActivo));

        // Act
        InsumoResponseDTO result = insumoService.buscarInsumoPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(insumoActivo.getIdInsumo(), result.getId());
        assertEquals(insumoActivo.getNombre(), result.getNombre());
        
        verify(insumoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Buscar Insumo por ID - Debería lanzar ResourceNotFoundException si el insumo no existe")
    void testBuscarInsumoPorId_NoExistente() {
        // Arrange
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: Espera que se lance una excepción
        assertThrows(ResourceNotFoundException.class, () -> insumoService.buscarInsumoPorId(99L));
        
        verify(insumoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Crear Insumo - Debería guardar un nuevo insumo correctamente")
    void testCrearInsumo_Exito() {
        // Arrange
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Nueva Jeringa", 100, new Date(), 2, true, 10L);
        
        // Simula el retorno del tipo de insumo
        when(tipoInsumoRepository.findById(10L)).thenReturn(Optional.of(tipoInsumoEjemplo));
        
        // Simula el guardado del insumo
        Insumo insumoAGuardar = new Insumo(); // Simula la creación del objeto
        // Copia propiedades del requestDTO a insumoAGuardar para la simulación
        insumoAGuardar.setNombre(requestDTO.getNombre());
        insumoAGuardar.setCantidadUsada(requestDTO.getCantidadUsada());
        insumoAGuardar.setFechaVencimiento(requestDTO.getFechaVencimiento());
        insumoAGuardar.setPrecioUnitario(requestDTO.getPrecioUnitario());
        insumoAGuardar.setEstado(requestDTO.getEstado());
        insumoAGuardar.setTipoInsumo(tipoInsumoEjemplo);
        // Cuando save es llamado con *cualquier* Insumo, devuelve una copia con ID asignado
        when(insumoRepository.save(any(Insumo.class))).thenAnswer(invocation -> {
            Insumo savedInsumo = invocation.getArgument(0);
            savedInsumo.setIdInsumo(3L); // Asigna un ID simulado para la respuesta
            return savedInsumo;
        });

        // Act
        InsumoResponseDTO result = insumoService.crearInsumo(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Nueva Jeringa", result.getNombre());
        assertTrue(result.getEstado());
        assertNotNull(result.getTipoInsumo());
        assertEquals("Medicamento", result.getTipoInsumo().getNombreTipo());

        verify(tipoInsumoRepository, times(1)).findById(10L);
        verify(insumoRepository, times(1)).save(any(Insumo.class));
    }

    @Test
    @DisplayName("Crear Insumo - Debería lanzar ResourceNotFoundException si el TipoInsumo no es encontrado")
    void testCrearInsumo_TipoInsumoNoEncontrado() {
        // Arrange
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Nueva Jeringa", 100, new Date(), 2, true, 99L); // ID de tipo insumo inexistente
        when(tipoInsumoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> insumoService.crearInsumo(requestDTO));
        
        verify(tipoInsumoRepository, times(1)).findById(99L);
        verify(insumoRepository, never()).save(any(Insumo.class)); // Asegura que save nunca fue llamado
    }

    @Test
    @DisplayName("Actualizar Insumo - Debería actualizar un insumo existente y retornar sus datos")
    void testActualizarInsumo_Exito() {
        // Arrange
        Long insumoId = 1L;
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Paracetamol Actualizado", 60, new Date(), 12, true, 10L);
        
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumoActivo));
        when(tipoInsumoRepository.findById(10L)).thenReturn(Optional.of(tipoInsumoEjemplo));
        when(insumoRepository.save(any(Insumo.class))).thenReturn(insumoActivo); // save retorna el insumo actualizado

        // Act
        InsumoResponseDTO result = insumoService.actualizarInsumo(insumoId, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(insumoId, result.getId());
        assertEquals("Paracetamol Actualizado", result.getNombre());
        assertEquals(60, result.getCantidadUsada());
        assertTrue(result.getEstado());
        assertEquals("Medicamento", result.getTipoInsumo().getNombreTipo());

        verify(insumoRepository, times(1)).findById(insumoId);
        verify(tipoInsumoRepository, times(1)).findById(10L);
        verify(insumoRepository, times(1)).save(insumoActivo); // Verifica que save fue llamado con la instancia de insumo actualizada
    }

    @Test
    @DisplayName("Actualizar Insumo - Debería lanzar ResourceNotFoundException si el insumo a actualizar no existe")
    void testActualizarInsumo_InsumoNoEncontrado() {
        // Arrange
        Long insumoId = 99L;
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Test", 1, new Date(), 1, true, 1L);
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> insumoService.actualizarInsumo(insumoId, requestDTO));
        
        verify(insumoRepository, times(1)).findById(insumoId);
        verify(tipoInsumoRepository, never()).findById(anyLong()); // No debe buscar tipo
        verify(insumoRepository, never()).save(any(Insumo.class)); // No debe guardar
    }

    @Test
    @DisplayName("Actualizar Insumo - Debería lanzar ResourceNotFoundException si el TipoInsumo asociado no existe")
    void testActualizarInsumo_TipoInsumoNoEncontrado() {
        // Arrange
        Long insumoId = 1L;
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Paracetamol", 50, new Date(), 10, true, 99L);
        
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumoActivo));
        when(tipoInsumoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> insumoService.actualizarInsumo(insumoId, requestDTO));
        
        verify(insumoRepository, times(1)).findById(insumoId);
        verify(tipoInsumoRepository, times(1)).findById(99L);
        verify(insumoRepository, never()).save(any(Insumo.class));
    }
    
    @Test
    @DisplayName("Eliminar Insumo - Debería cambiar el estado del insumo a inactivo (soft delete)")
    void testEliminarInsumo_Exito() {
        // Arrange
        Long insumoId = 1L;
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumoActivo));
        
        // Act
        insumoService.eliminarInsumo(insumoId);

        // Assert
        // Verifica que el estado del insumo activo se cambió a false
        assertFalse(insumoActivo.getEstado()); 
        // Verifica que findById fue llamado y que save fue llamado con el insumo actualizado (estado=false)
        verify(insumoRepository, times(1)).findById(insumoId);
        verify(insumoRepository, times(1)).save(insumoActivo);
    }

    @Test
    @DisplayName("Eliminar Insumo - Debería lanzar ResourceNotFoundException si el insumo a eliminar no existe")
    void testEliminarInsumo_NoEncontrado() {
        // Arrange
        Long insumoId = 99L;
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> insumoService.eliminarInsumo(insumoId));
        
        verify(insumoRepository, times(1)).findById(insumoId);
        verify(insumoRepository, never()).save(any(Insumo.class));
    }
}
