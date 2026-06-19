package com.example.demo.controller;

import com.example.demo.dto.CarritoRequestDTO;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Tag(name = "Gestión de Carrito", description = "Endpoints para el manejo de carritos de compras, agregar y actualizar items.")
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    @Operation(summary = "Obtener todos los carritos", description = "Retorna una colección de todos los carritos activos en el sistema con sus respectivos enlaces HATEOAS.")
    @ApiResponse(responseCode = "200", description = "Lista de carritos obtenida exitosamente")
    public ResponseEntity<CollectionModel<CarritoResponseDTO>> obtenerTodo() {
        List<CarritoResponseDTO> carritos = carritoService.obtenerTodos();
        // Agregar link "self" a cada carrito basado en el id de usuario
        carritos.forEach(carrito ->
                carrito.add(linkTo(methodOn(CarritoController.class)
                        .obtenerUsuarioId(carrito.getIdUsuario())) // Asumiendo que el DTO tiene getIdUsuario()
                        .withSelfRel()
                        .withTitle("Ver carrito del usuario"))
        );
        // Crear colección con link a sí misma
        CollectionModel<CarritoResponseDTO> resources = CollectionModel.of(carritos,
                linkTo(methodOn(CarritoController.class).obtenerTodo())
                        .withSelfRel()
                        .withTitle("Lista completa de carritos")
        );
        // Link hacia la creación de un carrito
        resources.add(linkTo(methodOn(CarritoController.class).guardarCarrito(null))
                .withRel("crear")
                .withTitle("Crear nuevo carrito"));

        return ResponseEntity.ok(resources);
    }
    @GetMapping("/{idUsuario}")
    @Operation(summary = "Obtener carrito por Usuario", description = "Busca y devuelve el carrito de compras asociado a un ID de usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado con éxito"),
            @ApiResponse(responseCode = "404", description = "El usuario no posee un carrito activo")
    })

    public ResponseEntity<CarritoResponseDTO> obtenerUsuarioId(@PathVariable Long idUsuario) {
        return carritoService.obtenerPorUsuarioId(idUsuario)
                .map(carrito -> {
                    // Links HATEOAS de navegación y acciones posibles
                    carrito.add(linkTo(methodOn(CarritoController.class)
                            .obtenerUsuarioId(idUsuario))
                            .withSelfRel()
                            .withTitle("Ver este carrito"));
                    carrito.add(linkTo(methodOn(CarritoController.class)
                            .obtenerTodo())
                            .withRel("carritos")
                            .withTitle("Ver todos los carritos"));
                    // Asumiendo que getIdCarrito() existe en el DTO
                    carrito.add(linkTo(methodOn(CarritoController.class)
                            .agregarItem(carrito.getIdCarrito(), null))
                            .withRel("agregar-item")
                            .withTitle("Añadir producto al carrito"));

                    return ResponseEntity.ok(carrito);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    @Operation(summary = "Crear un nuevo carrito", description = "Inicializa un carrito de compras para un usuario. Valida campos obligatorios de entrada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carrito creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en el formato de la petición")
    })

    public ResponseEntity<CarritoResponseDTO> guardarCarrito(@Valid @RequestBody CarritoRequestDTO dto) {
        CarritoResponseDTO nuevoCarrito = carritoService.guardar(dto);

        nuevoCarrito.add(linkTo(methodOn(CarritoController.class)
                .obtenerUsuarioId(nuevoCarrito.getIdUsuario()))
                .withSelfRel()
                .withTitle("Ver carrito creado"));
        nuevoCarrito.add(linkTo(methodOn(CarritoController.class)
                .obtenerTodo())
                .withRel("carritos")
                .withTitle("Lista de carritos"));

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCarrito);
    }
    @PostMapping("/{idCarrito}/items")
    @Operation(summary = "Agregar item al carrito", description = "Añade un nuevo producto al carrito o incrementa la cantidad si ya existe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item agregado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos del item inválidos"),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado")
    })

    public ResponseEntity<ItemCarritoResponseDTO> agregarItem(@PathVariable("idCarrito") Long idCarrito,
                                                              @Valid @RequestBody ItemCarritoRequestDTO dto) {
        ItemCarritoResponseDTO nuevoItem = carritoService.agregarItemAlCarrito(idCarrito, dto);

        nuevoItem.add(linkTo(methodOn(CarritoController.class)
                .agregarItem(idCarrito, dto))
                .withSelfRel()
                .withTitle("Operación de agregar item"));

        // Link directo para actualizar la cantidad de este ítem más adelante
        nuevoItem.add(linkTo(methodOn(CarritoController.class)
                .actualizarCantidadItem(idCarrito, nuevoItem.getIdProducto(), 0)) // 0 como parametro dummy
                .withRel("actualizar-cantidad")
                .withTitle("Actualizar cantidad de este item"));

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoItem);
    }
    @PutMapping("/{idCarrito}/items/{idProducto}")
    @Operation(summary = "Actualizar cantidad de un item", description = "Modifica la cantidad actual de un producto específico que se encuentra en el carrito.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada con éxito"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida (ej. número negativo)"),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado en el sistema")
    })

    public ResponseEntity<ItemCarritoResponseDTO> actualizarCantidadItem(
            @PathVariable("idCarrito") Long idCarrito,
            @PathVariable("idProducto") Long idProducto,
            @RequestParam("cantidad") Integer cantidad) {
        ItemCarritoResponseDTO itemActualizado = carritoService.actualizarCantidadItem(idCarrito, idProducto, cantidad);
        itemActualizado.add(linkTo(methodOn(CarritoController.class)
                .actualizarCantidadItem(idCarrito, idProducto, cantidad))
                .withSelfRel()
                .withTitle("Item actualizado"));

        itemActualizado.add(linkTo(methodOn(CarritoController.class)
                .eliminarItem(idCarrito, idProducto))
                .withRel("eliminar")
                .withTitle("Eliminar item del carrito"));

        return ResponseEntity.ok(itemActualizado);
    }

    @DeleteMapping("/{idCarrito}/items/{idProducto}")
    @Operation(summary = "Eliminar item del carrito", description = "Remueve permanentemente un producto específico del carrito.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item eliminado con éxito (Sin contenido)"),
            @ApiResponse(responseCode = "404", description = "El carrito o el producto indicado no existen")
    })
    public ResponseEntity<Void> eliminarItem(@PathVariable("idCarrito") Long idCarrito,
                                             @PathVariable("idProducto") Long idProducto) {
        // En Delete normalmente no mandamos HATEOAS porque devolvemos un Void 204 (No Content)
        carritoService.eliminarItemDelCarrito(idCarrito, idProducto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idCarrito}/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable("idCarrito") Long idCarrito) {
        carritoService.vaciarCarrito(idCarrito);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idCarrito}/procesar-pago")
    public ResponseEntity<String> procesarPago(@PathVariable("idCarrito") Long idCarrito) {
        String resultado = carritoService.procesarPago(idCarrito);
        return ResponseEntity.ok(resultado);
    }
}