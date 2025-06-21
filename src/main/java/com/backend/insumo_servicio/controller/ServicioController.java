package com.backend.insumo_servicio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.insumo_servicio.dto.ServicioRequestDTO;
import com.backend.insumo_servicio.dto.ServicioResponseDTO;
import com.backend.insumo_servicio.response.ApiResponse;
import com.backend.insumo_servicio.service.ServicioService;

@RestController
@RequestMapping("/api/servicio")
@CrossOrigin(origins = "*")
public class ServicioController {
    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponseDTO>>> getAll() {
        List<ServicioResponseDTO> lista = servicioService.obtenerServiciosActivos();
        return ResponseEntity.ok(ApiResponse.exito("Listado exitoso", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponseDTO>> getById(@PathVariable Long id) {
        ServicioResponseDTO dto = servicioService.buscarServicioPorId(id);
        return ResponseEntity.ok(ApiResponse.exito("Encontrado correctamente", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioResponseDTO>> create(@RequestBody ServicioRequestDTO dto) {
        ServicioResponseDTO creado = servicioService.crearServicio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.exito("Creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponseDTO>> update(@PathVariable Long id, @RequestBody ServicioRequestDTO dto) {
        ServicioResponseDTO actualizado = servicioService.actualizarServicio(id, dto);
        return ResponseEntity.ok(ApiResponse.exito("Actualizado correctamente", actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok(ApiResponse.exito("Eliminado correctamente", null));
    }
}
