package com.backend.insumo_servicio.service;

import java.util.List;

import com.backend.insumo_servicio.dto.ServicioRequestDTO;
import com.backend.insumo_servicio.dto.ServicioResponseDTO;

public interface ServicioService {
    List<ServicioResponseDTO> obtenerServiciosActivos();
    ServicioResponseDTO buscarServicioPorId(Long id);
    ServicioResponseDTO crearServicio(ServicioRequestDTO dto);
    ServicioResponseDTO actualizarServicio(Long id, ServicioRequestDTO dto);
    void eliminarServicio(Long id);
}
