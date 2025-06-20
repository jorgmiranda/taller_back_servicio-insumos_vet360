package com.backend.insumo_servicio.service;

import java.util.List;

import com.backend.insumo_servicio.dto.TipoInsumoDTO;

public interface TipoInsumoService {
    List<TipoInsumoDTO> obtenerTipoInsumosActivos();
    TipoInsumoDTO buscarTipoInsumoPorId(Long id);
    TipoInsumoDTO crearTipoInsumo(TipoInsumoDTO dto);
    TipoInsumoDTO actualizarTipoInsumo(Long id, TipoInsumoDTO dto);
    void eliminarTipoInsumo(Long id);
}
