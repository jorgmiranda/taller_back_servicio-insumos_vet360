package com.backend.insumo_servicio.service;

import java.util.List;

import com.backend.insumo_servicio.dto.TipoInsumoDTO;

public interface TipoInsumoService {
    List<TipoInsumoDTO> obtenerTipoInsumosActivos();
    TipoInsumoDTO buscarTipoInsumoPorId(Integer id);
    TipoInsumoDTO crearTipoInsumo(TipoInsumoDTO dto);
    TipoInsumoDTO actualizarTipoInsumo(Integer id, TipoInsumoDTO dto);
    void eliminarTipoInsumo(Integer id);
}
