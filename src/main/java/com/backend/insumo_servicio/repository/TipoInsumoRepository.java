package com.backend.insumo_servicio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.insumo_servicio.model.TipoInsumo;

public interface TipoInsumoRepository extends JpaRepository<TipoInsumo, Integer> {
}
