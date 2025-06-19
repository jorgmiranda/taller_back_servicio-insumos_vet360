package com.backend.insumo_servicio.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Long idDisponibilidad;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date fecha;

    @Column(name = "hora_inicio", nullable = false)
    private Double horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private Double horaFin;

    @Column(nullable = false)
    private Boolean disponible;

    @Column(name = "username_keycloak", nullable = false, length = 30)
    private String usernameKeycloak;

    public Disponibilidad() {
    }

    public Disponibilidad(Long idDisponibilidad, Date fecha, Double horaInicio, Double horaFin,
            Boolean disponible, String usernameKeycloak) {
        this.idDisponibilidad = idDisponibilidad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = disponible;
        this.usernameKeycloak = usernameKeycloak;
    }

    public Long getIdDisponibilidad() {
        return idDisponibilidad;
    }

    public void setIdDisponibilidad(Long idDisponibilidad) {
        this.idDisponibilidad = idDisponibilidad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Double horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Double getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Double horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getUsernameKeycloak() {
        return usernameKeycloak;
    }

    public void setUsernameKeycloak(String usernameKeycloak) {
        this.usernameKeycloak = usernameKeycloak;
    }

}
