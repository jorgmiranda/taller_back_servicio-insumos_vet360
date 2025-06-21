package com.backend.insumo_servicio.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.insumo_servicio.dto.InsumoResponseDTO;
import com.backend.insumo_servicio.dto.ServicioRequestDTO;
import com.backend.insumo_servicio.dto.ServicioResponseDTO;
import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.model.Insumo;
import com.backend.insumo_servicio.model.Servicio;
import com.backend.insumo_servicio.repository.InsumoRepository;
import com.backend.insumo_servicio.repository.ServicioRepository;
import com.backend.insumo_servicio.service.ServicioService;

@Service
public class ServicioServiceImpl implements ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Override
    public List<ServicioResponseDTO> obtenerServiciosActivos() {
        return servicioRepository.findAll().stream()
                .filter(Servicio::getEstado)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServicioResponseDTO buscarServicioPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        return convertirADTO(servicio);
    }

    @Override
    public ServicioResponseDTO crearServicio(ServicioRequestDTO dto) {
        Servicio servicio = new Servicio();
        servicio.setNombreServicio(dto.getNombreServicio());
        servicio.setDescripcionServicio(dto.getDescripcionServicio());
        servicio.setPrecio(dto.getPrecio());
        servicio.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        servicio.setInsumos(obtenerInsumosDesdeIds(dto.getInsumoIds()));
        return convertirADTO(servicioRepository.save(servicio));
    }

    @Override
    public ServicioResponseDTO actualizarServicio(Long id, ServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        servicio.setNombreServicio(dto.getNombreServicio());
        servicio.setDescripcionServicio(dto.getDescripcionServicio());
        servicio.setPrecio(dto.getPrecio());
        servicio.setEstado(dto.getEstado());

        if (dto.getInsumoIds() != null) {
            servicio.setInsumos(obtenerInsumosDesdeIds(dto.getInsumoIds()));
        }

        return convertirADTO(servicioRepository.save(servicio));
    }

    @Override
    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        servicio.setEstado(false);
        servicioRepository.save(servicio);
    }

    private ServicioResponseDTO convertirADTO(Servicio servicio) {
        ServicioResponseDTO dto = new ServicioResponseDTO();
        dto.setId(servicio.getIdServicio());
        dto.setNombreServicio(servicio.getNombreServicio());
        dto.setDescripcionServicio(servicio.getDescripcionServicio());
        dto.setPrecio(servicio.getPrecio());
        dto.setEstado(servicio.getEstado());

        List<InsumoResponseDTO> insumos = servicio.getInsumos().stream().map(insumo -> {
            InsumoResponseDTO i = new InsumoResponseDTO();
            i.setId(insumo.getIdInsumo());
            i.setNombre(insumo.getNombre());
            i.setCantidadUsada(insumo.getCantidadUsada());
            i.setFechaVencimiento(insumo.getFechaVencimiento());
            i.setPrecioUnitario(insumo.getPrecioUnitario());
            i.setEstado(insumo.getEstado());

            // ✅ Agregar tipoInsumo
            if (insumo.getTipoInsumo() != null) {
                TipoInsumoDTO tipoDTO = new TipoInsumoDTO();
                tipoDTO.setId(insumo.getTipoInsumo().getIdTipoInsumo());
                tipoDTO.setNombreTipo(insumo.getTipoInsumo().getNombreTipo());
                tipoDTO.setEstado(insumo.getTipoInsumo().getEstado());
                i.setTipoInsumo(tipoDTO);
            }

            return i;
        }).collect(Collectors.toList());

        dto.setInsumos(insumos);
        return dto;
    }

    private List<Insumo> obtenerInsumosDesdeIds(List<Long> insumoIds) {
        return insumoIds.stream()
                .map(id -> insumoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Insumo con ID " + id + " no encontrado")))
                .collect(Collectors.toList());
    }
}
