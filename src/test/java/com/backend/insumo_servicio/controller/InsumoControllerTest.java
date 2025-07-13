package com.backend.insumo_servicio.controller;

import com.backend.insumo_servicio.dto.InsumoRequestDTO;
import com.backend.insumo_servicio.dto.InsumoResponseDTO;
import com.backend.insumo_servicio.dto.TipoInsumoDTO;
import com.backend.insumo_servicio.exception.ResourceNotFoundException;
import com.backend.insumo_servicio.service.InsumoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod; // Nueva importación
import org.springframework.http.HttpStatus;
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
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsumoController.class)
@Import(InsumoControllerTest.TestSecurityConfig.class)
//@AutoConfigureMockMvc(addFilters = false)
public class InsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsumoService insumoService;

    @Autowired
    private ObjectMapper objectMapper;

    private InsumoResponseDTO insumoResponseDTO1;
    private InsumoResponseDTO insumoResponseDTO2;
    private TipoInsumoDTO tipoInsumoDTOEjemplo;

    @BeforeEach
    void setUp() {
        tipoInsumoDTOEjemplo = new TipoInsumoDTO(10L, "Medicamento", true);
        
        insumoResponseDTO1 = new InsumoResponseDTO(1L, "Paracetamol", 50, new Date(), 10, true, tipoInsumoDTOEjemplo);
        insumoResponseDTO2 = new InsumoResponseDTO(2L, "Vendas", 20, new Date(), 5, true, tipoInsumoDTOEjemplo);
    }

    // --- Pruebas para GET /api/insumo (obtener todos activos) ---
    @Test
    @DisplayName("GET /api/insumo - Con rol ADMIN, debería retornar una lista de insumos")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAll_ConAdmin_RetornaLista() throws Exception {
        List<InsumoResponseDTO> insumosActivos = Arrays.asList(insumoResponseDTO1, insumoResponseDTO2);
        when(insumoService.obtenerInsumosActivos()).thenReturn(insumosActivos);

        mockMvc.perform(get("/api/insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Listado exitoso"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].nombre").value("Paracetamol"));

        verify(insumoService, times(1)).obtenerInsumosActivos();
    }

   

    @Test
    // REMOVIDO @WithMockUser: Se prueba sin autenticación
    @DisplayName("GET /api/insumo - Sin autenticación, debería retornar 401 Unauthorized")
    void testGetAll_SinAutenticacion_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/insumo")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        verify(insumoService, never()).obtenerInsumosActivos();
    }

    // --- Pruebas para GET /api/insumo/{id} (buscar por ID) ---
    @Test
    @DisplayName("GET /api/insumo/{id} - Con rol VETERINARIO y ID existente, debería retornar el insumo")
    @WithMockUser(username = "vet", roles = {"VETERINARIO"})
    void testGetById_Existente_RetornaInsumo() throws Exception {
        when(insumoService.buscarInsumoPorId(1L)).thenReturn(insumoResponseDTO1);

        mockMvc.perform(get("/api/insumo/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.nombre").value("Paracetamol"));

        verify(insumoService, times(1)).buscarInsumoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/insumo/{id} - Con rol ADMIN y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetById_NoExistente_RetornaNotFound() throws Exception {
        when(insumoService.buscarInsumoPorId(99L)).thenThrow(new ResourceNotFoundException("Insumo no encontrado"));

        mockMvc.perform(get("/api/insumo/{id}", 99L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Insumo no encontrado"));

        verify(insumoService, times(1)).buscarInsumoPorId(99L);
    }

    // --- Pruebas para POST /api/insumo (crear insumo) ---
    @Test
    @DisplayName("POST /api/insumo - Con rol ADMIN, debería crear un insumo y retornar 201 Created")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreate_ConAdmin_RetornaCreado() throws Exception {
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Nuevo Medicamento", 10, new Date(), 20, true, 10L);
        InsumoResponseDTO createdResponseDTO = new InsumoResponseDTO(3L, "Nuevo Medicamento", 10, new Date(), 20, true, tipoInsumoDTOEjemplo);

        when(insumoService.crearInsumo(any(InsumoRequestDTO.class))).thenReturn(createdResponseDTO);

        mockMvc.perform(post("/api/insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Creado correctamente"))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.nombre").value("Nuevo Medicamento"));

        verify(insumoService, times(1)).crearInsumo(any(InsumoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/insumo - Con rol ASISTENTE, debería crear un insumo y retornar 201 Created")
    @WithMockUser(username = "asistente", roles = {"ASISTENTE"})
    void testCreate_ConAsistente_RetornaCreado() throws Exception {
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Nuevo Medicamento", 10, new Date(), 20, true, 10L);
        InsumoResponseDTO createdResponseDTO = new InsumoResponseDTO(3L, "Nuevo Medicamento", 10, new Date(), 20, true, tipoInsumoDTOEjemplo);

        when(insumoService.crearInsumo(any(InsumoRequestDTO.class))).thenReturn(createdResponseDTO);

        mockMvc.perform(post("/api/insumo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("exito"));

        verify(insumoService, times(1)).crearInsumo(any(InsumoRequestDTO.class));
    }


    // --- Pruebas para PUT /api/insumo/{id} (actualizar insumo) ---
    @Test
    @DisplayName("PUT /api/insumo/{id} - Con rol VETERINARIO y ID existente, debería actualizar el insumo y retornar 200 OK")
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    void testUpdate_ConVeterinario_RetornaActualizado() throws Exception {
        Long insumoId = 1L;
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("Paracetamol Actualizado", 60, new Date(), 12, true, 10L);
        InsumoResponseDTO updatedResponseDTO = new InsumoResponseDTO(insumoId, "Paracetamol Actualizado", 60, new Date(), 12, true, tipoInsumoDTOEjemplo);

        when(insumoService.actualizarInsumo(eq(insumoId), any(InsumoRequestDTO.class))).thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/api/insumo/{id}", insumoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.data.nombre").value("Paracetamol Actualizado"))
                .andExpect(jsonPath("$.data.cantidadUsada").value(60));

        verify(insumoService, times(1)).actualizarInsumo(eq(insumoId), any(InsumoRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/insumo/{id} - Con rol ADMIN y ID no existente, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdate_InsumoNoEncontrado_RetornaNotFound() throws Exception {
        Long insumoId = 99L;
        InsumoRequestDTO requestDTO = new InsumoRequestDTO("No Existe", 1, new Date(), 1, true, 10L);
        when(insumoService.actualizarInsumo(eq(insumoId), any(InsumoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Insumo no encontrado para actualizar"));

        mockMvc.perform(put("/api/insumo/{id}", insumoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(insumoService, times(1)).actualizarInsumo(eq(insumoId), any(InsumoRequestDTO.class));
    }

    // --- Pruebas para DELETE /api/insumo/{id} (eliminar insumo - cambia estado a inactivo) ---
    @Test
    @DisplayName("DELETE /api/insumo/{id} - Con rol ADMIN, debería eliminar (desactivar) el insumo y retornar 200 OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_ConAdmin_Exito() throws Exception {
        Long insumoId = 1L;
        doNothing().when(insumoService).eliminarInsumo(insumoId);

        mockMvc.perform(delete("/api/insumo/{id}", insumoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("exito"))
                .andExpect(jsonPath("$.message").value("Eliminado correctamente"));

        verify(insumoService, times(1)).eliminarInsumo(insumoId);
    }

    @Test
    @DisplayName("DELETE /api/insumo/{id} - Con rol ADMIN y ID no encontrado, debería retornar 404 Not Found")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDelete_NoEncontrado_RetornaNotFound() throws Exception {
        Long insumoId = 99L;
        doThrow(new ResourceNotFoundException("Insumo no encontrado para eliminar"))
                .when(insumoService).eliminarInsumo(insumoId);

        mockMvc.perform(delete("/api/insumo/{id}", insumoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));

        verify(insumoService, times(1)).eliminarInsumo(insumoId);
    }

    // --- Configuración de seguridad para los tests de @WebMvcTest ---
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
                    .requestMatchers(HttpMethod.GET, "/api/insumo", "/api/insumo/{id}").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/insumo").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/insumo/{id}").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/insumo/{id}").authenticated()
                    .anyRequest().authenticated() 
                )
                .httpBasic(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                    // Manejar la denegación de acceso para que devuelva 403 en los tests
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        // Opcional: Escribir un cuerpo de error JSON similar al de tu app real
                        String errorBody = "{\"status\":\"error\",\"message\":\"Acceso denegado. No tienes los permisos necesarios.\",\"data\":null}";
                        response.getWriter().write(errorBody);
                    })
                );

            return http.build();
        }
    }

}
