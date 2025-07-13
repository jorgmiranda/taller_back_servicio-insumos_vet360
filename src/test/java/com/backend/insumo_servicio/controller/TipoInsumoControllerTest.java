package com.backend.insumo_servicio.controller;

import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.exception.ResourceNotFoundException; // Asume que tienes esta excepción
import com.backend.insumo_servicio.service.TipoInsumoService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoInsumoController.class)
@Import(TipoInsumoControllerTest.TestSecurityConfig.class)
public class TipoInsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TipoInsumoService tipoInsumoService;

    @Autowired
    private ObjectMapper objectMapper;

    private TipoInsumoDTO tipoInsumoDTO1;

    @BeforeEach
    void setUp() {
        tipoInsumoDTO1 = new TipoInsumoDTO(1L, "Medicamento", true);
    }

    // --- Pruebas para GET /api/tipo-insumo (obtener todos activos) ---
    @Test
    @DisplayName("GET /api/tipo-insumo - Con rol ADMIN, debería retornar una lista de tipos de insumo activos")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAll_ConAdmin_RetornaListaActivos() throws Exception {
        List<TipoInsumoDTO> activos = Arrays.asList(tipoInsumoDTO1); // Solo activos
        when(tipoInsumoService.obtenerTipoInsumosActivos()).thenReturn(activos);

        mockMvc.perform(get("/api/tipo-insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Listado exitoso"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].nombreTipo").value("Medicamento"));

        verify(tipoInsumoService, times(1)).obtenerTipoInsumosActivos();
    }

    @Test
    @DisplayName("GET /api/tipo-insumo - Con rol VETERINARIO, debería retornar una lista de tipos de insumo activos")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testGetAll_ConVeterinario_RetornaListaActivos() throws Exception {
        List<TipoInsumoDTO> activos = Arrays.asList(tipoInsumoDTO1);
        when(tipoInsumoService.obtenerTipoInsumosActivos()).thenReturn(activos);

        mockMvc.perform(get("/api/tipo-insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"));
        verify(tipoInsumoService, times(1)).obtenerTipoInsumosActivos();
    }

    @Test
    @DisplayName("GET /api/tipo-insumo - Sin autenticación, debería retornar 401 Unauthorized")
    void testGetAll_SinAutenticacion_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/tipo-insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(tipoInsumoService, never()).obtenerTipoInsumosActivos();
    }

    @Test
    @DisplayName("GET /api/tipo-insumo - Con rol no autorizado (ej. USER), debería retornar 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAll_ConRolNoAutorizado_Forbidden() throws Exception {
        mockMvc.perform(get("/api/tipo-insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tipoInsumoService, never()).obtenerTipoInsumosActivos();
    }


    // --- Pruebas para GET /api/tipo-insumo/{id} (buscar por ID) ---
    @Test
    @DisplayName("GET /api/tipo-insumo/{id} - Con rol ASISTENTE y ID existente, debería retornar el tipo de insumo")
    @WithMockUser(username = "asistente", roles = {"ASISTENTE"})
    void testGetById_Existente_RetornaTipoInsumo() throws Exception {
        when(tipoInsumoService.buscarTipoInsumoPorId(1L)).thenReturn(tipoInsumoDTO1);

        mockMvc.perform(get("/api/tipo-insumo/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.nombreTipo").value("Medicamento"));

        verify(tipoInsumoService, times(1)).buscarTipoInsumoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/tipo-insumo/{id} - Con rol ADMIN y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetById_NoExistente_RetornaNotFound() throws Exception {
        when(tipoInsumoService.buscarTipoInsumoPorId(99L)).thenThrow(new ResourceNotFoundException("TipoInsumo no encontrado"));

        mockMvc.perform(get("/api/tipo-insumo/{id}", 99L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("TipoInsumo no encontrado"));

        verify(tipoInsumoService, times(1)).buscarTipoInsumoPorId(99L);
    }

    // --- Pruebas para POST /api/tipo-insumo (crear tipo de insumo) ---
    @Test
    @DisplayName("POST /api/tipo-insumo - Con rol ADMIN, debería crear un tipo de insumo y retornar 201 Created")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreate_ConAdmin_RetornaCreado() throws Exception {
        TipoInsumoDTO requestDTO = new TipoInsumoDTO(null, "Alimento", true);
        TipoInsumoDTO createdResponseDTO = new TipoInsumoDTO(3L, "Alimento", true);

        when(tipoInsumoService.crearTipoInsumo(any(TipoInsumoDTO.class))).thenReturn(createdResponseDTO);

        mockMvc.perform(post("/api/tipo-insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Creado correctamente"))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.nombreTipo").value("Alimento"));

        verify(tipoInsumoService, times(1)).crearTipoInsumo(any(TipoInsumoDTO.class));
    }

    @Test
    @DisplayName("POST /api/tipo-insumo - Sin autenticación, debería retornar 401 Unauthorized")
    void testCreate_SinAutenticacion_Unauthorized() throws Exception {
        TipoInsumoDTO requestDTO = new TipoInsumoDTO(null, "Alimento", true);

        mockMvc.perform(post("/api/tipo-insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
        verify(tipoInsumoService, never()).crearTipoInsumo(any(TipoInsumoDTO.class));
    }

    @Test
    @DisplayName("POST /api/tipo-insumo - Con rol no autorizado (ej. USER), debería retornar 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testCreate_ConRolNoAutorizado_Forbidden() throws Exception {
        TipoInsumoDTO requestDTO = new TipoInsumoDTO(null, "Alimento", true);

        mockMvc.perform(post("/api/tipo-insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
        verify(tipoInsumoService, never()).crearTipoInsumo(any(TipoInsumoDTO.class));
    }

    // --- Pruebas para PUT /api/tipo-insumo/{id} (actualizar tipo de insumo) ---
    @Test
    @DisplayName("PUT /api/tipo-insumo/{id} - Con rol ASISTENTE y ID existente, debería actualizar el tipo de insumo")
    @WithMockUser(username = "asistente", roles = {"ASISTENTE"})
    void testUpdate_ConAsistente_RetornaActualizado() throws Exception {
        Long idToUpdate = 1L;
        TipoInsumoDTO requestDTO = new TipoInsumoDTO(idToUpdate, "Medicamento (Editado)", false);
        TipoInsumoDTO updatedResponseDTO = new TipoInsumoDTO(idToUpdate, "Medicamento (Editado)", false);

        when(tipoInsumoService.actualizarTipoInsumo(eq(idToUpdate), any(TipoInsumoDTO.class))).thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/tipo-insumo/{id}", idToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.nombreTipo").value("Medicamento (Editado)"))
                .andExpect(jsonPath("$.data.estado").value(false));

        verify(tipoInsumoService, times(1)).actualizarTipoInsumo(eq(idToUpdate), any(TipoInsumoDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tipo-insumo/{id} - Con rol VETERINARIO y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testUpdate_TipoInsumoNoEncontrado_RetornaNotFound() throws Exception {
        Long idToUpdate = 99L;
        TipoInsumoDTO requestDTO = new TipoInsumoDTO(idToUpdate, "No Existe", true);
        when(tipoInsumoService.actualizarTipoInsumo(eq(idToUpdate), any(TipoInsumoDTO.class)))
                .thenThrow(new ResourceNotFoundException("TipoInsumo no encontrado"));

        mockMvc.perform(put("/api/tipo-insumo/{id}", idToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(tipoInsumoService, times(1)).actualizarTipoInsumo(eq(idToUpdate), any(TipoInsumoDTO.class));
    }

    // --- Pruebas para DELETE /api/tipo-insumo/{id} (eliminar tipo de insumo - cambia estado a inactivo) ---
    @Test
    @DisplayName("DELETE /api/tipo-insumo/{id} - Con rol ADMIN, debería eliminar (desactivar) el tipo de insumo")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_ConAdmin_Exito() throws Exception {
        Long idToDelete = 1L;
        doNothing().when(tipoInsumoService).eliminarTipoInsumo(idToDelete);

        mockMvc.perform(delete("/api/tipo-insumo/{id}", idToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Eliminado correctamente"));

        verify(tipoInsumoService, times(1)).eliminarTipoInsumo(idToDelete);
    }

    @Test
    @DisplayName("DELETE /api/tipo-insumo/{id} - Con rol ADMIN y ID no encontrado, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_NoEncontrado_RetornaNotFound() throws Exception {
        Long idToDelete = 99L;
        doThrow(new ResourceNotFoundException("TipoInsumo no encontrado para eliminar"))
                .when(tipoInsumoService).eliminarTipoInsumo(idToDelete);

        mockMvc.perform(delete("/api/tipo-insumo/{id}", idToDelete))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(tipoInsumoService, times(1)).eliminarTipoInsumo(idToDelete);
    }

    @Test
    @DisplayName("DELETE /api/tipo-insumo/{id} - Con rol VETERINARIO, debería retornar 403 Forbidden")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testDelete_ConVeterinario_Forbidden() throws Exception {
        Long idToDelete = 1L;

        mockMvc.perform(delete("/api/tipo-insumo/{id}", idToDelete))
                .andExpect(status().isForbidden());
        verify(tipoInsumoService, never()).eliminarTipoInsumo(anyLong());
    }

    // --- Configuración de seguridad para los tests de @WebMvcTest ---
    // Esta clase TestSecurityConfig es crucial para que los tests de controlador con Spring Security funcionen.
    // Debería ser muy similar a la que usas en InsumoControllerTest.
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
                    // Definir las autorizaciones para cada endpoint
                    .requestMatchers(HttpMethod.GET, "/api/tipo-insumo", "/api/tipo-insumo/{id}").hasAnyRole("ADMIN", "VETERINARIO", "ASISTENTE")
                    .requestMatchers(HttpMethod.POST, "/api/tipo-insumo").hasAnyRole("ADMIN", "VETERINARIO", "ASISTENTE")
                    .requestMatchers(HttpMethod.PUT, "/api/tipo-insumo/{id}").hasAnyRole("ADMIN", "VETERINARIO", "ASISTENTE")
                    .requestMatchers(HttpMethod.DELETE, "/api/tipo-insumo/{id}").hasRole("ADMIN") // Solo ADMIN puede eliminar
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
