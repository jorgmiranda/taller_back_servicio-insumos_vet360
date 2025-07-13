package com.backend.insumo_servicio.service;

import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.model.TipoInsumo;
import com.backend.insumo_servicio.repository.TipoInsumoRepository;
import com.backend.insumo_servicio.service.impl.TipoInsumoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoInsumoServiceImplTest {

     @Mock
    private TipoInsumoRepository tipoInsumoRepository;

    @InjectMocks
    private TipoInsumoServiceImpl tipoInsumoService;

    private TipoInsumo tipoInsumoActivo;
    private TipoInsumo tipoInsumoInactivo;
    private TipoInsumoDTO tipoInsumoDTOActivo;


    @BeforeEach
    void setUp() {
        tipoInsumoActivo = new TipoInsumo(1L, "Medicamento", true);
        tipoInsumoInactivo = new TipoInsumo(2L, "Material Quirúrgico", false);

        tipoInsumoDTOActivo = new TipoInsumoDTO(1L, "Medicamento", true);
    }

    @Test
    @DisplayName("Obtener Tipos de Insumo Activos - Debería retornar una lista filtrada por estado true")
    void testObtenerTipoInsumosActivos() {
        // Arrange
        List<TipoInsumo> allTipoInsumos = Arrays.asList(tipoInsumoActivo, tipoInsumoInactivo);
        when(tipoInsumoRepository.findAll()).thenReturn(allTipoInsumos);

        // Act
        List<TipoInsumoDTO> result = tipoInsumoService.obtenerTipoInsumosActivos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tipoInsumoDTOActivo.getNombreTipo(), result.get(0).getNombreTipo());
        assertTrue(result.get(0).getEstado());
        verify(tipoInsumoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar Tipo de Insumo por ID - Debería retornar el DTO si el tipo de insumo existe")
    void testBuscarTipoInsumoPorId_Existente() {
        // Arrange
        when(tipoInsumoRepository.findById(1L)).thenReturn(Optional.of(tipoInsumoActivo));

        // Act
        TipoInsumoDTO result = tipoInsumoService.buscarTipoInsumoPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(tipoInsumoActivo.getIdTipoInsumo(), result.getId());
        assertEquals(tipoInsumoActivo.getNombreTipo(), result.getNombreTipo());
        verify(tipoInsumoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Buscar Tipo de Insumo por ID - Debería lanzar ResourceNotFoundException si el tipo de insumo no existe")
    void testBuscarTipoInsumoPorId_NoExistente() {
        // Arrange
        when(tipoInsumoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> tipoInsumoService.buscarTipoInsumoPorId(99L));
        assertEquals("TipoInsumo no encontrado", exception.getMessage());
        verify(tipoInsumoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Crear Tipo de Insumo - Debería guardar un nuevo tipo de insumo correctamente")
    void testCrearTipoInsumo_Exito() {
        // Arrange
        TipoInsumoDTO newTipoInsumoDTO = new TipoInsumoDTO(null, "Vacuna", true);
        TipoInsumo savedTipoInsumo = new TipoInsumo(3L, "Vacuna", true);

        when(tipoInsumoRepository.save(any(TipoInsumo.class))).thenReturn(savedTipoInsumo);

        // Act
        TipoInsumoDTO result = tipoInsumoService.crearTipoInsumo(newTipoInsumoDTO);

        // Assert
        assertNotNull(result);
        assertEquals(savedTipoInsumo.getIdTipoInsumo(), result.getId());
        assertEquals(newTipoInsumoDTO.getNombreTipo(), result.getNombreTipo());
        assertTrue(result.getEstado());
        verify(tipoInsumoRepository, times(1)).save(any(TipoInsumo.class));
    }

    @Test
    @DisplayName("Actualizar Tipo de Insumo - Debería modificar un tipo de insumo existente")
    void testActualizarTipoInsumo_Exito() {
        // Arrange
        Long idToUpdate = 1L;
        TipoInsumoDTO updatedTipoInsumoDTO = new TipoInsumoDTO(idToUpdate, "Medicamento (Actualizado)", false);

        when(tipoInsumoRepository.findById(idToUpdate)).thenReturn(Optional.of(tipoInsumoActivo));
        when(tipoInsumoRepository.save(any(TipoInsumo.class))).thenReturn(new TipoInsumo(idToUpdate, "Medicamento (Actualizado)", false));

        // Act
        TipoInsumoDTO result = tipoInsumoService.actualizarTipoInsumo(idToUpdate, updatedTipoInsumoDTO);

        // Assert
        assertNotNull(result);
        assertEquals(idToUpdate, result.getId());
        assertEquals("Medicamento (Actualizado)", result.getNombreTipo());
        assertFalse(result.getEstado());
        verify(tipoInsumoRepository, times(1)).findById(idToUpdate);
        verify(tipoInsumoRepository, times(1)).save(any(TipoInsumo.class));
    }

    @Test
    @DisplayName("Actualizar Tipo de Insumo - Debería lanzar ResourceNotFoundException si el tipo de insumo no existe")
    void testActualizarTipoInsumo_NoExistente() {
        // Arrange
        Long idToUpdate = 99L;
        TipoInsumoDTO updatedTipoInsumoDTO = new TipoInsumoDTO(idToUpdate, "Inexistente", true);
        when(tipoInsumoRepository.findById(idToUpdate)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> tipoInsumoService.actualizarTipoInsumo(idToUpdate, updatedTipoInsumoDTO));
        assertEquals("TipoInsumo no encontrado", exception.getMessage());
        verify(tipoInsumoRepository, times(1)).findById(idToUpdate);
        verify(tipoInsumoRepository, never()).save(any(TipoInsumo.class));
    }

    @Test
    @DisplayName("Eliminar Tipo de Insumo - Debería cambiar el estado a false (eliminación lógica)")
    void testEliminarTipoInsumo_Exito() {
        // Arrange
        Long idToDelete = 1L;
        TipoInsumo tipoInsumoToDeactivate = new TipoInsumo(idToDelete, "Medicamento", true);
        when(tipoInsumoRepository.findById(idToDelete)).thenReturn(Optional.of(tipoInsumoToDeactivate));

        // Act
        tipoInsumoService.eliminarTipoInsumo(idToDelete);

        // Assert
        assertFalse(tipoInsumoToDeactivate.getEstado()); // Verifica que el estado se actualizó en el objeto mock
        verify(tipoInsumoRepository, times(1)).findById(idToDelete);
        verify(tipoInsumoRepository, times(1)).save(tipoInsumoToDeactivate); // Verifica que el objeto modificado fue guardado
    }

    @Test
    @DisplayName("Eliminar Tipo de Insumo - Debería lanzar ResourceNotFoundException si el tipo de insumo no existe")
    void testEliminarTipoInsumo_NoExistente() {
        // Arrange
        Long idToDelete = 99L;
        when(tipoInsumoRepository.findById(idToDelete)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> tipoInsumoService.eliminarTipoInsumo(idToDelete));
        assertEquals("TipoInsumo no encontrado", exception.getMessage());
        verify(tipoInsumoRepository, times(1)).findById(idToDelete);
        verify(tipoInsumoRepository, never()).save(any(TipoInsumo.class));
    }
}
