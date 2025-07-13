package com.backend.insumo_servicio.controller;

import com.backend.insumo_servicio.dto.InsumoResponseDTO;
import com.backend.insumo_servicio.dto.ServicioRequestDTO;
import com.backend.insumo_servicio.dto.ServicioResponseDTO;
import com.backend.insumo_servicio.dto.TipoInsumoDTO; // Necesaria para InsumoResponseDTO
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.service.ServicioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServicioController.class)
@Import(ServicioControllerTest.TestSecurityConfig.class)
public class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioService servicioService;

    @Autowired
    private ObjectMapper objectMapper;

    private ServicioResponseDTO servicioDTO1;
    private ServicioResponseDTO servicioDTO2;
    private InsumoResponseDTO insumoDTO1;
    private TipoInsumoDTO tipoInsumoDTO;

    @BeforeEach
    void setUp() {
        // Inicializar DTOs anidados
        tipoInsumoDTO = new TipoInsumoDTO(1L, "Medicamento", true);
        
        insumoDTO1 = new InsumoResponseDTO();
        insumoDTO1.setId(100L);
        insumoDTO1.setNombre("Paracetamol");
        insumoDTO1.setCantidadUsada(50);
        insumoDTO1.setFechaVencimiento(new Date());
        insumoDTO1.setPrecioUnitario(10);
        insumoDTO1.setEstado(true);
        insumoDTO1.setTipoInsumo(tipoInsumoDTO);

        servicioDTO1 = new ServicioResponseDTO();
        servicioDTO1.setId(1L);
        servicioDTO1.setNombreServicio("Consulta Veterinaria");
        servicioDTO1.setDescripcionServicio("Consulta general para mascotas");
        servicioDTO1.setPrecio(30000);
        servicioDTO1.setEstado(true);
        servicioDTO1.setInsumos(Arrays.asList(insumoDTO1));

        servicioDTO2 = new ServicioResponseDTO();
        servicioDTO2.setId(2L);
        servicioDTO2.setNombreServicio("Vacunación Perros");
        servicioDTO2.setDescripcionServicio("Vacuna antirrábica");
        servicioDTO2.setPrecio(25000);
        servicioDTO2.setEstado(false); // Inactivo
        servicioDTO2.setInsumos(Collections.emptyList());
    }

    // --- Pruebas para GET /api/servicio (obtener todos activos) ---
    @Test
    @DisplayName("GET /api/servicio - Con rol ADMIN, debería retornar una lista de servicios activos")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAll_ConAdmin_RetornaListaActivos() throws Exception {
        List<ServicioResponseDTO> activos = Arrays.asList(servicioDTO1);
        when(servicioService.obtenerServiciosActivos()).thenReturn(activos);

        mockMvc.perform(get("/api/servicio")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Listado exitoso"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].nombreServicio").value("Consulta Veterinaria"));

        verify(servicioService, times(1)).obtenerServiciosActivos();
    }

    @Test
    @DisplayName("GET /api/servicio - Sin autenticación, debería retornar 401 Unauthorized")
    void testGetAll_SinAutenticacion_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/servicio")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(servicioService, never()).obtenerServiciosActivos();
    }

    @Test
    @DisplayName("GET /api/servicio - Con rol no autorizado (ej. USER), debería retornar 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAll_ConRolNoAutorizado_Forbidden() throws Exception {
        mockMvc.perform(get("/api/servicio")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(servicioService, never()).obtenerServiciosActivos();
    }

    // --- Pruebas para GET /api/servicio/{id} (buscar por ID) ---
    @Test
    @DisplayName("GET /api/servicio/{id} - Con rol VETERINARIO y ID existente, debería retornar el servicio")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testGetById_Existente_RetornaServicio() throws Exception {
        when(servicioService.buscarServicioPorId(1L)).thenReturn(servicioDTO1);

        mockMvc.perform(get("/api/servicio/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.nombreServicio").value("Consulta Veterinaria"))
                .andExpect(jsonPath("$.data.insumos[0].nombre").value("Paracetamol")); // Verificar insumos

        verify(servicioService, times(1)).buscarServicioPorId(1L);
    }

    @Test
    @DisplayName("GET /api/servicio/{id} - Con rol ADMIN y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetById_NoExistente_RetornaNotFound() throws Exception {
        when(servicioService.buscarServicioPorId(99L)).thenThrow(new ResourceNotFoundException("Servicio no encontrado"));

        mockMvc.perform(get("/api/servicio/{id}", 99L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Servicio no encontrado"));

        verify(servicioService, times(1)).buscarServicioPorId(99L);
    }

    // --- Pruebas para POST /api/servicio (crear servicio) ---
    @Test
    @DisplayName("POST /api/servicio - Con rol ADMIN, debería crear un servicio y retornar 201 Created")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreate_ConAdmin_RetornaCreado() throws Exception {
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Peluquería Canina");
        requestDTO.setDescripcionServicio("Corte de pelo y baño");
        requestDTO.setPrecio(40000);
        requestDTO.setEstado(true);
        requestDTO.setInsumoIds(Arrays.asList(100L)); // Asocia con un insumo existente

        ServicioResponseDTO createdResponseDTO = new ServicioResponseDTO();
        createdResponseDTO.setId(3L);
        createdResponseDTO.setNombreServicio("Peluquería Canina");
        createdResponseDTO.setDescripcionServicio("Corte de pelo y baño");
        createdResponseDTO.setPrecio(40000);
        createdResponseDTO.setEstado(true);
        createdResponseDTO.setInsumos(Arrays.asList(insumoDTO1));

        when(servicioService.crearServicio(any(ServicioRequestDTO.class))).thenReturn(createdResponseDTO);

        mockMvc.perform(post("/api/servicio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Creado correctamente"))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.nombreServicio").value("Peluquería Canina"))
                .andExpect(jsonPath("$.data.insumos[0].nombre").value("Paracetamol"));

        verify(servicioService, times(1)).crearServicio(any(ServicioRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/servicio - Sin autenticación, debería retornar 401 Unauthorized")
    void testCreate_SinAutenticacion_Unauthorized() throws Exception {
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Peluquería");
        mockMvc.perform(post("/api/servicio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
        verify(servicioService, never()).crearServicio(any(ServicioRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/servicio - Con rol no autorizado (ej. ASISTENTE), debería retornar 403 Forbidden")
    @WithMockUser(username = "asistente", roles = {"ASISTENTE"})
    void testCreate_ConRolNoAutorizado_Forbidden() throws Exception {
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Peluquería");
        mockMvc.perform(post("/api/servicio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
        verify(servicioService, never()).crearServicio(any(ServicioRequestDTO.class));
    }

    // --- Pruebas para PUT /api/servicio/{id} (actualizar servicio) ---
    @Test
    @DisplayName("PUT /api/servicio/{id} - Con rol ADMIN y ID existente, debería actualizar el servicio")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdate_ConAdmin_RetornaActualizado() throws Exception {
        Long idToUpdate = 1L;
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Consulta de Emergencia");
        requestDTO.setDescripcionServicio("Atención urgente 24/7");
        requestDTO.setPrecio(60000);
        requestDTO.setEstado(true);
        requestDTO.setInsumoIds(Collections.emptyList()); // Quitar insumos

        ServicioResponseDTO updatedResponseDTO = new ServicioResponseDTO();
        updatedResponseDTO.setId(idToUpdate);
        updatedResponseDTO.setNombreServicio("Consulta de Emergencia");
        updatedResponseDTO.setDescripcionServicio("Atención urgente 24/7");
        updatedResponseDTO.setPrecio(60000);
        updatedResponseDTO.setEstado(true);
        updatedResponseDTO.setInsumos(Collections.emptyList());

        when(servicioService.actualizarServicio(eq(idToUpdate), any(ServicioRequestDTO.class))).thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/servicio/{id}", idToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.nombreServicio").value("Consulta de Emergencia"))
                .andExpect(jsonPath("$.data.insumos").isEmpty());

        verify(servicioService, times(1)).actualizarServicio(eq(idToUpdate), any(ServicioRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/servicio/{id} - Con rol ADMIN y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdate_ServicioNoEncontrado_RetornaNotFound() throws Exception {
        Long idToUpdate = 99L;
        ServicioRequestDTO requestDTO = new ServicioRequestDTO();
        requestDTO.setNombreServicio("Servicio Inexistente");
        when(servicioService.actualizarServicio(eq(idToUpdate), any(ServicioRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Servicio no encontrado para actualizar"));

        mockMvc.perform(put("/api/servicio/{id}", idToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(servicioService, times(1)).actualizarServicio(eq(idToUpdate), any(ServicioRequestDTO.class));
    }

    // --- Pruebas para DELETE /api/servicio/{id} (eliminar servicio - cambia estado a inactivo) ---
    @Test
    @DisplayName("DELETE /api/servicio/{id} - Con rol ADMIN, debería eliminar (desactivar) el servicio")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_ConAdmin_Exito() throws Exception {
        Long idToDelete = 1L;
        doNothing().when(servicioService).eliminarServicio(idToDelete);

        mockMvc.perform(delete("/api/servicio/{id}", idToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Eliminado correctamente"));

        verify(servicioService, times(1)).eliminarServicio(idToDelete);
    }

    @Test
    @DisplayName("DELETE /api/servicio/{id} - Con rol ADMIN y ID no encontrado, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_NoEncontrado_RetornaNotFound() throws Exception {
        Long idToDelete = 99L;
        doThrow(new ResourceNotFoundException("Servicio no encontrado para eliminar"))
                .when(servicioService).eliminarServicio(idToDelete);

        mockMvc.perform(delete("/api/servicio/{id}", idToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(servicioService, times(1)).eliminarServicio(idToDelete);
    }

    @Test
    @DisplayName("DELETE /api/servicio/{id} - Con rol VETERINARIO, debería retornar 403 Forbidden")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testDelete_ConVeterinario_Forbidden() throws Exception {
        Long idToDelete = 1L;

        mockMvc.perform(delete("/api/servicio/{id}", idToDelete))
                .andExpect(status().isForbidden());
        verify(servicioService, never()).eliminarServicio(anyLong());
    }


    // --- Configuración de seguridad para los tests de @WebMvcTest ---
    // Esta clase TestSecurityConfig es crucial para que los tests de controlador con Spring Security funcionen.
    @org.springframework.boot.test.context.TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails admin = User.withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
            UserDetails veterinario = User.withUsername("veterinario")
                .password("{noop}password")
                .roles("VETERINARIO")
                .build();
            UserDetails asistente = User.withUsername("asistente")
                .password("{noop}password")
                .roles("ASISTENTE")
                .build();
            UserDetails user = User.withUsername("user")
                .password("{noop}password")
                .roles("USER")
                .build();
            
            return new InMemoryUserDetailsManager(admin, veterinario, asistente, user);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers(HttpMethod.GET, "/api/servicio", "/api/servicio/{id}").hasAnyRole("ADMIN", "VETERINARIO")
                    .requestMatchers(HttpMethod.POST, "/api/servicio").hasRole("ADMIN") // Solo ADMIN para crear
                    .requestMatchers(HttpMethod.PUT, "/api/servicio/{id}").hasRole("ADMIN") // Solo ADMIN para actualizar
                    .requestMatchers(HttpMethod.DELETE, "/api/servicio/{id}").hasRole("ADMIN") // Solo ADMIN para eliminar
                    .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                    // Manejar la denegación de acceso para que devuelva 403 en los tests
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        String errorBody = "{\"status\":\"error\",\"message\":\"Acceso denegado. No tienes los permisos necesarios.\",\"data\":null}";
                        response.getWriter().write(errorBody);
                    })
                );

            return http.build();
        }
    }
}
