package com.example.demo.serviceTest.serviceTest;

import com.example.demo.client.CatalogoClient;
import com.example.demo.client.UsuarioClient;
import com.example.demo.dto.CarritoRequestDTO;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.model.Carrito;
import com.example.demo.model.ItemCarrito;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.ItemCarritoRepository;
import com.example.demo.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

// 👑 LOS ÚNICOS IMPORTS ESTÁTICOS QUE NECESITAS PARA LAS ASERCIONES Y MOCKS:
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest { // <-- Corregido el nombre para que coincida con lo que se testea

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito carritoEjemplo;
    private ItemCarrito itemEjemplo;

    @BeforeEach
    void setUp() {
        carritoEjemplo = new Carrito();
        carritoEjemplo.setIdCarrito(1L);
        carritoEjemplo.setIdUsuario(42L);
        carritoEjemplo.setFechaCreacion(LocalDateTime.now());

        itemEjemplo = new ItemCarrito();
        itemEjemplo.setIdItem(10L);
        itemEjemplo.setCarrito(carritoEjemplo);
        itemEjemplo.setIdProducto(100L);
        itemEjemplo.setCantidad(2);
    }

    @Test
    @DisplayName("guardar() -> Debe crear un carrito si el usuario existe de forma remota")
    void guardar_UsuarioExiste_CreaCarrito() {
        // ARRANGE
        CarritoRequestDTO request = new CarritoRequestDTO();
        request.setIdUsuario(42L);

        when(usuarioClient.obtenerUsuarioPorId(42L)).thenReturn(new Object());
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoEjemplo);

        // ACT
        CarritoResponseDTO resultado = carritoService.guardar(request);

        // ASSERT
        assertNotNull(resultado); // Ahora usa el assertNotNull correcto de JUnit
        assertEquals(42L, resultado.getIdUsuario());
        verify(usuarioClient, times(1)).obtenerUsuarioPorId(42L);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    @DisplayName("guardar() -> Debe lanzar excepción si el usuario remoto no existe")
    void guardar_UsuarioNoExiste_LanzaExcepcion() {
        // ARRANGE
        CarritoRequestDTO request = new CarritoRequestDTO();
        request.setIdUsuario(999L);

        when(usuarioClient.obtenerUsuarioPorId(999L)).thenReturn(null);

        // ACT & ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            carritoService.guardar(request);
        });

        assertTrue(excepcion.getMessage().contains("no existe"));
        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarCantidadItem() -> Debe lanzar IllegalArgumentException si la cantidad es cero o menor")
    void actualizarCantidadItem_CantidadInvalida_LanzaExcepcion() {
        // ACT & ASSERT
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            carritoService.actualizarCantidadItem(1L, 100L, 0);
        });

        assertEquals("La cantidad debe ser mayor a cero", excepcion.getMessage());
        verify(itemCarritoRepository, never()).findByCarrito_IdCarritoAndIdProducto(any(), any());
    }

    @Test
    @DisplayName("agregarItemAlCarrito() -> Debe lanzar excepción si el producto remoto no existe en el catálogo")
    void agregarItem_ProductoNoExisteEnCatalogo_LanzaExcepcion() {
        // ARRANGE
        ItemCarritoRequestDTO requestItem = new ItemCarritoRequestDTO();
        requestItem.setIdProducto(500L);
        requestItem.setCantidad(1);

        when(catalogoClient.obtenerProductoPorId(500L)).thenReturn(null);

        // ACT & ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarItemAlCarrito(1L, requestItem);
        });

        assertTrue(excepcion.getMessage().contains("no existe en el catálogo"));
        verify(carritoRepository, never()).findById(any());
    }
}