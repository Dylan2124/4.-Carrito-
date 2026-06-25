package com.example.demo.serviceTest;

import com.example.demo.client.CatalogoClient;
import com.example.demo.client.UsuarioClient;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.dto.ProductoResponseDTO;
import com.example.demo.model.Carrito;
import com.example.demo.model.ItemCarrito;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.ItemCarritoRepository;
import com.example.demo.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

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

    private Carrito carrito;
    private ItemCarrito itemExistente;

    @BeforeEach
    void setUp() {
        // Preparar datos comunes para los tests
        carrito = new Carrito();
        carrito.setIdCarrito(1L);
        carrito.setIdUsuario(42L);
        carrito.setFechaCreacion(LocalDateTime.now());
        carrito.setItems(new ArrayList<>());

        itemExistente = new ItemCarrito();
        itemExistente.setIdItem(10L);
        itemExistente.setIdProducto(100L);
        itemExistente.setCantidad(2);
        itemExistente.setCarrito(carrito);
    }

    /**     * TEST 1: Verificar que al agregar un item existente, la cantidad se suma correctamente.     * Estructura: Arrange - Act - Assert (AAA)     */
    @Test
    void testAgregarItemExistente_SumaCantidades() {
        // ========== ARRANGE (Preparar datos) ==========
        Long idCarrito = 1L;
        Long idProducto = 100L;
        Integer cantidadNueva = 3;

        ItemCarritoRequestDTO requestDTO = new ItemCarritoRequestDTO();
        requestDTO.setIdProducto(idProducto);
        requestDTO.setCantidad(cantidadNueva);

        carrito.getItems().add(itemExistente);

        // Mock: simulamos que el carrito existe
        when(carritoRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));

        // Mock: simulamos que el producto existe en catálogo
        when(catalogoClient.obtenerProductoPorId(idProducto)).thenReturn(mock(ProductoResponseDTO.class));
        // Mock: simulamos que el item existe en el repositorio
        when(itemCarritoRepository.findByCarrito_IdCarritoAndIdProducto(idCarrito, idProducto))
                .thenReturn(Optional.of(itemExistente));

        // Mock: simulamos el guardado del item actualizado
        ItemCarrito itemActualizado = new ItemCarrito();
        itemActualizado.setIdItem(10L);
        itemActualizado.setCarrito(carrito);
        itemActualizado.setIdProducto(idProducto);
        itemActualizado.setCantidad(5); // 2 (existente) + 3 (nuevo) = 5
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenReturn(itemActualizado);

        // ========== ACT (Ejecutar la acción) ==========
        ItemCarritoResponseDTO resultado = carritoService.agregarItemAlCarrito(idCarrito, requestDTO);

        // ========== ASSERT (Comprobar que el resultado es correcto) ==========
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(5, resultado.getCantidad().intValue(),
                "La cantidad debe ser 5 (2 + 3)");
        assertEquals(idProducto, resultado.getIdProducto(),
                "El ID del producto debe coincidir");
        assertEquals(idCarrito, resultado.getIdCarrito(),
                "El ID del carrito debe coincidir");

        // Verificar que se llamaron los métodos esperados
        verify(carritoRepository, times(1)).findById(idCarrito);
        verify(catalogoClient, times(1)).obtenerProductoPorId(idProducto);
        verify(itemCarritoRepository, times(1)).findByCarrito_IdCarritoAndIdProducto(idCarrito, idProducto);
        verify(itemCarritoRepository, times(1)).save(any(ItemCarrito.class));
    }

    /**     * TEST 2: Verificar que al agregar un nuevo item (que no existe), se crea correctamente.     * Estructura: Arrange - Act - Assert (AAA)     */
    @Test
    void testAgregarItemNuevo_CreaCorrectamente() {
        // ========== ARRANGE (Preparar datos) ==========
        Long idCarrito = 1L;
        Long idProductoNuevo = 200L;
        Integer cantidad = 5;

        ItemCarritoRequestDTO requestDTO = new ItemCarritoRequestDTO();
        requestDTO.setIdProducto(idProductoNuevo);
        requestDTO.setCantidad(cantidad);

        // Mock: simulamos que el carrito existe
        when(carritoRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));

        // Mock: simulamos que el producto existe en catálogo
        when(catalogoClient.obtenerProductoPorId(idProductoNuevo)).thenReturn(mock(ProductoResponseDTO.class));
        // Mock: simulamos que el item NO existe (Optional.empty())
        when(itemCarritoRepository.findByCarrito_IdCarritoAndIdProducto(idCarrito, idProductoNuevo))
                .thenReturn(Optional.empty());

        // Mock: simulamos el guardado del item nuevo
        ItemCarrito itemNuevo = new ItemCarrito();
        itemNuevo.setIdItem(20L);
        itemNuevo.setCarrito(carrito);
        itemNuevo.setIdProducto(idProductoNuevo);
        itemNuevo.setCantidad(cantidad);
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenReturn(itemNuevo);

        // ========== ACT (Ejecutar la acción) ==========
        ItemCarritoResponseDTO resultado = carritoService.agregarItemAlCarrito(idCarrito, requestDTO);

        // ========== ASSERT (Comprobar que el resultado es correcto) ==========
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(cantidad, resultado.getCantidad().intValue(),
                "La cantidad debe ser la ingresada (" + cantidad + ")");
        assertEquals(idProductoNuevo, resultado.getIdProducto(),
                "El ID del producto debe coincidir");
        assertEquals(20L, resultado.getIdItem().longValue(),
                "El ID del item debe ser el asignado por la BD");

        // Verificar que se llamaron los métodos esperados
        verify(carritoRepository, times(1)).findById(idCarrito);
        verify(catalogoClient, times(1)).obtenerProductoPorId(idProductoNuevo);
        verify(itemCarritoRepository, times(1)).findByCarrito_IdCarritoAndIdProducto(idCarrito, idProductoNuevo);
        verify(itemCarritoRepository, times(1)).save(any(ItemCarrito.class));
    }

    /**     * TEST 3: Verificar que la lógica maneja correctamente cuando el carrito no existe.     */
    @Test
    void testAgregarItem_CarritoNoExiste_LanzaExcepcion() {
        // ========== ARRANGE ==========
        Long idCarritoNoExiste = 999L;
        ItemCarritoRequestDTO requestDTO = new ItemCarritoRequestDTO();
        requestDTO.setIdProducto(100L);
        requestDTO.setCantidad(1);

        // Mock: simulamos que el carrito NO existe
        when(carritoRepository.findById(idCarritoNoExiste)).thenReturn(Optional.empty());
        when(catalogoClient.obtenerProductoPorId(100L)).thenReturn(mock(ProductoResponseDTO.class));
        // ========== ACT & ASSERT ==========
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarItemAlCarrito(idCarritoNoExiste, requestDTO);
        });

        assertTrue(excepcion.getMessage().contains("Carrito no encontrado"),
                "Debe lanzar excepción indicando que el carrito no existe");
    }

    /**     * TEST 4: Verificar que valida correctamente una cantidad inválida (negativa o cero).     */
    @Test
    void testActualizarCantidad_CantidadInvalida_LanzaExcepcion() {
        // ========== ARRANGE ==========
        Long idCarrito = 1L;
        Long idProducto = 100L;
        Integer cantidadInvalida = 0;

        when(itemCarritoRepository.findByCarrito_IdCarritoAndIdProducto(idCarrito, idProducto))
                .thenReturn(Optional.of(itemExistente));

        // ========== ACT & ASSERT ==========
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            carritoService.actualizarCantidadItem(idCarrito, idProducto, cantidadInvalida);
        });

        assertTrue(excepcion.getMessage().contains("mayor a cero"),
                "Debe validar que la cantidad sea mayor a cero");
    }

}
