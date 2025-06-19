package com.backend.insumo_servicio.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.model.TipoInsumo;
import com.backend.insumo_servicio.repository.TipoInsumoRepository;
import com.backend.insumo_servicio.service.TipoInsumoService;

@Service
public class TipoInsumoServiceImpl implements TipoInsumoService {
    @Autowired
    private TipoInsumoRepository tipoInsumoRepository;

    @Override
    public List<TipoInsumoDTO> obtenerTipoInsumosActivos() {
        return tipoInsumoRepository.findAll().stream()
                .filter(TipoInsumo::getEstado)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public TipoInsumoDTO buscarTipoInsumoPorId(Integer id) {
        TipoInsumo entity = tipoInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TipoInsumo no encontrado"));
        return convertirADTO(entity);
    }

    @Override
    public TipoInsumoDTO crearTipoInsumo(TipoInsumoDTO dto) {
        TipoInsumo entity = new TipoInsumo();
        System.out.println(dto.getNombreTipo());
        System.out.println(dto.getEstado());
        entity.setNombreTipo(dto.getNombreTipo());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        return convertirADTO(tipoInsumoRepository.save(entity));
    }

    @Override
    public TipoInsumoDTO actualizarTipoInsumo(Integer id, TipoInsumoDTO dto) {
        TipoInsumo existente = tipoInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TipoInsumo no encontrado"));

        existente.setNombreTipo(dto.getNombreTipo());
        existente.setEstado(dto.getEstado());
        return convertirADTO(tipoInsumoRepository.save(existente));
    }

    @Override
    public void eliminarTipoInsumo(Integer id) {
        TipoInsumo tipoInsumo = tipoInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TipoInsumo no encontrado"));
        tipoInsumo.setEstado(false);
        tipoInsumoRepository.save(tipoInsumo);
    }

    private TipoInsumoDTO convertirADTO(TipoInsumo entity) {
        TipoInsumoDTO dto = new TipoInsumoDTO();
        dto.setId(entity.getIdTipoInsumo());
        dto.setNombreTipo(entity.getNombreTipo());
        dto.setEstado(entity.getEstado());
        return dto;
    }
}
