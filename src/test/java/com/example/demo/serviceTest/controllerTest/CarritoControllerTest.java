package com.example.demo.serviceTest.controllerTest;

import com.example.demo.dto.CarritoRequestDTO;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
@DisplayName("Pruebas del Controlador de Carrito (Capa Web)")
class CarritoControllerTest {

    // 💡 NO lleva @Autowired. Lo inicializamos manualmente en el setUp para que no tire error de contexto.
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean // Cambia por @MockBean si tu versión de Spring es previa a la 3.4
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 👑 INICIALIZACIÓN CONFIGURADA DE FORMA MANUAL: Salta todos los filtros problemáticos.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /api/carrito -> Debe retornar todos los carritos con formato HATEOAS")
    void obtenerTodo_DebeRetornarColeccionHateoas() throws Exception {
        // ARRANGE
        CarritoResponseDTO carritoMock = new CarritoResponseDTO(1L, 42L, LocalDateTime.now(), Collections.emptyList());
        when(carritoService.obtenerTodos()).thenReturn(List.of(carritoMock));

        // ACT & ASSERT
        mockMvc.perform(get("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 💡 CORRECCIÓN: Apuntar al nombre exacto de la lista de HATEOAS
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.carritoResponseDTOList[0].idCarrito").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.carritoResponseDTOList[0].idUsuario").value(42L))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());

        verify(carritoService, times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/carrito/{idUsuario} -> Debe retornar 200 OK si el usuario tiene un carrito")
    void obtenerUsuarioId_Existente_RetornaCarrito() throws Exception {
        // ARRANGE
        CarritoResponseDTO carritoMock = new CarritoResponseDTO(1L, 42L, LocalDateTime.now(), Collections.emptyList());
        when(carritoService.obtenerPorUsuarioId(42L)).thenReturn(Optional.of(carritoMock));

        // ACT & ASSERT
        mockMvc.perform(get("/api/carrito/42")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCarrito").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());

        verify(carritoService, times(1)).obtenerPorUsuarioId(42L);
    }

    @Test
    @DisplayName("GET /api/carrito/{idUsuario} -> Debe retornar 404 Not Found si el usuario no tiene carrito")
    void obtenerUsuarioId_Inexistente_RetornaNotFound() throws Exception {
        // ARRANGE
        when(carritoService.obtenerPorUsuarioId(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/carrito/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(carritoService, times(1)).obtenerPorUsuarioId(99L);
    }

    @Test
    @DisplayName("POST /api/carrito -> Debe crear un carrito y retornar 201 Created")
    void guardarCarrito_Valido_RetornaCreado() throws Exception {
        // ARRANGE
        CarritoRequestDTO requestDTO = new CarritoRequestDTO();
        requestDTO.setIdUsuario(42L);

        CarritoResponseDTO responseDTO = new CarritoResponseDTO(1L, 42L, LocalDateTime.now(), Collections.emptyList());
        when(carritoService.guardar(any(CarritoRequestDTO.class))).thenReturn(responseDTO);

        // ACT & ASSERT
        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCarrito").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());

        verify(carritoService, times(1)).guardar(any(CarritoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/carrito/{id}/items -> Debe agregar un producto y retornar 201 Created")
    void agregarItem_Valido_RetornaItemCreado() throws Exception {
        // ARRANGE
        ItemCarritoRequestDTO requestItem = new ItemCarritoRequestDTO();
        requestItem.setIdProducto(100L);
        requestItem.setCantidad(3);

        ItemCarritoResponseDTO responseItem = new ItemCarritoResponseDTO(10L, 1L, 100L, 3);
        when(carritoService.agregarItemAlCarrito(eq(1L), any(ItemCarritoRequestDTO.class))).thenReturn(responseItem);

        // ACT & ASSERT
        mockMvc.perform(post("/api/carrito/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestItem)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idItem").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$._links.self.href").exists());

        verify(carritoService, times(1)).agregarItemAlCarrito(eq(1L), any(ItemCarritoRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/carrito/{id}/items/{idProd} -> Debe actualizar la cantidad (200 OK)")
    void actualizarCantidadItem_Valido_RetornaOk() throws Exception {
        // ARRANGE
        ItemCarritoResponseDTO responseItem = new ItemCarritoResponseDTO(10L, 1L, 100L, 5);
        when(carritoService.actualizarCantidadItem(1L, 100L, 5)).thenReturn(responseItem);

        // ACT & ASSERT
        mockMvc.perform(put("/api/carrito/1/items/100")
                        .param("cantidad", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.cantidad").value(5));

        verify(carritoService, times(1)).actualizarCantidadItem(1L, 100L, 5);
    }

    @Test
    @DisplayName("DELETE /api/carrito/{id}/items/{idProd} -> Debe eliminar el item (204 No Content)")
    void eliminarItem_Existente_RetornaNoContent() throws Exception {
        // ARRANGE
        doNothing().when(carritoService).eliminarItemDelCarrito(1L, 100L);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/carrito/1/items/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(carritoService, times(1)).eliminarItemDelCarrito(1L, 100L);
    }
}