package com.backend.insumo_servicio.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.insumo_servicio.dto.InsumoRequestDTO;
import com.backend.insumo_servicio.dto.InsumoResponseDTO;
import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.model.Insumo;
import com.backend.insumo_servicio.model.TipoInsumo;
import com.backend.insumo_servicio.repository.InsumoRepository;
import com.backend.insumo_servicio.repository.TipoInsumoRepository;
import com.backend.insumo_servicio.service.InsumoService;

@Service
public class InsumoServiceImpl implements InsumoService {
    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private TipoInsumoRepository tipoInsumoRepository;

    @Override
    public List<InsumoResponseDTO> obtenerInsumosActivos() {
        return insumoRepository.findAll().stream()
                .filter(Insumo::getEstado)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public InsumoResponseDTO buscarInsumoPorId(Long id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        return convertirADTO(insumo);
    }

    @Override
    public InsumoResponseDTO crearInsumo(InsumoRequestDTO dto) {
        TipoInsumo tipo = tipoInsumoRepository.findById(dto.getTipoInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de insumo no encontrado"));

        Insumo insumo = new Insumo();
        insumo.setNombre(dto.getNombre());
        insumo.setCantidadUsada(dto.getCantidadUsada());
        insumo.setFechaVencimiento(dto.getFechaVencimiento());
        insumo.setPrecioUnitario(dto.getPrecioUnitario());
        insumo.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        insumo.setTipoInsumo(tipo);

        return convertirADTO(insumoRepository.save(insumo));
    }

    @Override
    public InsumoResponseDTO actualizarInsumo(Long id, InsumoRequestDTO dto) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));

        insumo.setNombre(dto.getNombre());
        insumo.setCantidadUsada(dto.getCantidadUsada());
        insumo.setFechaVencimiento(dto.getFechaVencimiento());
        insumo.setPrecioUnitario(dto.getPrecioUnitario());
        insumo.setEstado(dto.getEstado());

        if (dto.getTipoInsumoId() != null) {
            TipoInsumo tipo = tipoInsumoRepository.findById(dto.getTipoInsumoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de insumo no encontrado"));
            insumo.setTipoInsumo(tipo);
        }

        return convertirADTO(insumoRepository.save(insumo));
    }

    @Override
    public void eliminarInsumo(Long id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        insumo.setEstado(false);
        insumoRepository.save(insumo);
    }

    private InsumoResponseDTO convertirADTO(Insumo insumo) {
        TipoInsumo tipo = insumo.getTipoInsumo();
        TipoInsumoDTO tipoDTO = new TipoInsumoDTO();
        tipoDTO.setId(tipo.getIdTipoInsumo());
        tipoDTO.setNombreTipo(tipo.getNombreTipo());
        tipoDTO.setEstado(tipo.getEstado());

        InsumoResponseDTO dto = new InsumoResponseDTO();
        dto.setId(insumo.getIdInsumo());
        dto.setNombre(insumo.getNombre());
        dto.setCantidadUsada(insumo.getCantidadUsada());
        dto.setFechaVencimiento(insumo.getFechaVencimiento());
        dto.setPrecioUnitario(insumo.getPrecioUnitario());
        dto.setEstado(insumo.getEstado());
        dto.setTipoInsumo(tipoDTO);
        return dto;
    }
}
