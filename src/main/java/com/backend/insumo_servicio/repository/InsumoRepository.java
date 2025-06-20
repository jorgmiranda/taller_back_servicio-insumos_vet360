package com.backend.insumo_servicio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.insumo_servicio.model.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
}
