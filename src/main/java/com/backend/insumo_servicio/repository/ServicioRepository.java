package com.backend.insumo_servicio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.insumo_servicio.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long>{
    
}
