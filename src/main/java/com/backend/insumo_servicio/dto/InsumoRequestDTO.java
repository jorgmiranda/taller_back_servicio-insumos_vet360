package com.backend.insumo_servicio.dto;

import java.util.Date;

public class InsumoRequestDTO {
    private String nombre;
    private Integer cantidadUsada;
    private Date fechaVencimiento;
    private Integer precioUnitario;
    private Boolean estado;
    private Long tipoInsumoId;
    public InsumoRequestDTO() {
    }
    public InsumoRequestDTO(String nombre, Integer cantidadUsada, Date fechaVencimiento, Integer precioUnitario,
            Boolean estado, Long tipoInsumoId) {
        this.nombre = nombre;
        this.cantidadUsada = cantidadUsada;
        this.fechaVencimiento = fechaVencimiento;
        this.precioUnitario = precioUnitario;
        this.estado = estado;
        this.tipoInsumoId = tipoInsumoId;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Integer getCantidadUsada() {
        return cantidadUsada;
    }
    public void setCantidadUsada(Integer cantidadUsada) {
        this.cantidadUsada = cantidadUsada;
    }
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }
    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
    public Integer getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(Integer precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    public Boolean getEstado() {
        return estado;
    }
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    public Long getTipoInsumoId() {
        return tipoInsumoId;
    }
    public void setTipoInsumoId(Long tipoInsumoId) {
        this.tipoInsumoId = tipoInsumoId;
    }

    
}
