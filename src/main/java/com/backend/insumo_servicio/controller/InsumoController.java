package com.backend.insumo_servicio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.insumo_servicio.dto.InsumoRequestDTO;
import com.backend.insumo_servicio.dto.InsumoResponseDTO;
import com.backend.insumo_servicio.response.ApiResponse;
import com.backend.insumo_servicio.service.InsumoService;

@RestController
@RequestMapping("/api/insumo")
@CrossOrigin(origins = "*")
public class InsumoController {
    @Autowired
    private InsumoService insumoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO', 'ASISTENTE')")
    public ResponseEntity<ApiResponse<List<InsumoResponseDTO>>> getAll() {
        List<InsumoResponseDTO> lista = insumoService.obtenerInsumosActivos();
        return ResponseEntity.ok(ApiResponse.exito("Listado exitoso", lista));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO', 'ASISTENTE')")
    public ResponseEntity<ApiResponse<InsumoResponseDTO>> getById(@PathVariable Long id) {
        InsumoResponseDTO dto = insumoService.buscarInsumoPorId(id);
        return ResponseEntity.ok(ApiResponse.exito("Encontrado correctamente", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO', 'ASISTENTE')")
    public ResponseEntity<ApiResponse<InsumoResponseDTO>> create(@RequestBody InsumoRequestDTO dto) {
        InsumoResponseDTO creado = insumoService.crearInsumo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.exito("Creado correctamente", creado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO', 'ASISTENTE')")
    public ResponseEntity<ApiResponse<InsumoResponseDTO>> update(@PathVariable Long id, @RequestBody InsumoRequestDTO dto) {
        InsumoResponseDTO actualizado = insumoService.actualizarInsumo(id, dto);
        return ResponseEntity.ok(ApiResponse.exito("Actualizado correctamente", actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        insumoService.eliminarInsumo(id);
        return ResponseEntity.ok(ApiResponse.exito("Eliminado correctamente", null));
    }
}
