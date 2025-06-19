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

import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.response.ApiResponse;
import com.backend.insumo_servicio.service.TipoInsumoService;

@RestController
@RequestMapping("/api/tipo-insumo")
@CrossOrigin(origins = "*")
public class TipoInsumoController {
    @Autowired
    private TipoInsumoService tipoInsumoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoInsumoDTO>>> getAll() {
        List<TipoInsumoDTO> lista = tipoInsumoService.obtenerTipoInsumosActivos();
        return ResponseEntity.ok(ApiResponse.exito("Listado exitoso", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoInsumoDTO>> getById(@PathVariable Integer id) {
        TipoInsumoDTO dto = tipoInsumoService.buscarTipoInsumoPorId(id);
        return ResponseEntity.ok(ApiResponse.exito("Encontrado correctamente", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoInsumoDTO>> create(@RequestBody TipoInsumoDTO dto) {
        TipoInsumoDTO creado = tipoInsumoService.crearTipoInsumo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.exito("Creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoInsumoDTO>> update(@PathVariable Integer id, @RequestBody TipoInsumoDTO dto) {
        TipoInsumoDTO actualizado = tipoInsumoService.actualizarTipoInsumo(id, dto);
        return ResponseEntity.ok(ApiResponse.exito("Actualizado correctamente", actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        tipoInsumoService.eliminarTipoInsumo(id);
        return ResponseEntity.ok(ApiResponse.exito("Eliminado correctamente", null));
    }
}
