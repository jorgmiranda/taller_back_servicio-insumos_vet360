package com.backend.insumo_servicio.service;

import java.util.List;

import com.backend.insumo_servicio.dto.InsumoRequestDTO;
import com.backend.insumo_servicio.dto.InsumoResponseDTO;

public interface InsumoService {
    List<InsumoResponseDTO> obtenerInsumosActivos();
    InsumoResponseDTO buscarInsumoPorId(Long id);
    InsumoResponseDTO crearInsumo(InsumoRequestDTO dto);
    InsumoResponseDTO actualizarInsumo(Long id, InsumoRequestDTO dto);
    void eliminarInsumo(Long id);
}
