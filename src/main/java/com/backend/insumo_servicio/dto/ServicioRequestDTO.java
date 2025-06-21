package com.backend.insumo_servicio.dto;

import java.util.List;

public class ServicioRequestDTO {
    private String nombreServicio;
    private String descripcionServicio;
    private Integer precio;
    private Boolean estado;
    private List<Long> insumoIds;

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }

    public Integer getPrecio() {
        return precio;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public List<Long> getInsumoIds() {
        return insumoIds;
    }

    public void setInsumoIds(List<Long> insumoIds) {
        this.insumoIds = insumoIds;
    }
}
