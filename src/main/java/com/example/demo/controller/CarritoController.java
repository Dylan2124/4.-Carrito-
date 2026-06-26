package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Tag(name = "Gestión de Carrito")
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    @Operation(summary = "Obtener todos los carritos")
    public ResponseEntity<CollectionModel<CarritoResponseDTO>> obtenerTodo() {
        List<CarritoResponseDTO> carritos = carritoService.obtenerTodos();

        carritos.forEach(carrito -> carrito.add(
                linkTo(methodOn(CarritoController.class).obtenerUsuarioId(carrito.getIdUsuario())).withSelfRel()
        ));

        CollectionModel<CarritoResponseDTO> modelo = CollectionModel.of(carritos);
        modelo.add(linkTo(methodOn(CarritoController.class).obtenerTodo()).withSelfRel());

        modelo.add(linkTo(methodOn(CarritoController.class).guardarCarrito(new CarritoRequestDTO())).withRel("crear"));

        return ResponseEntity.ok(modelo);
    }

    @GetMapping("/{idUsuario}")
    @Operation(summary = "Obtener carrito por Usuario")
    public ResponseEntity<CarritoResponseDTO> obtenerUsuarioId(@PathVariable Long idUsuario) {
        return carritoService.obtenerPorUsuarioId(idUsuario)
                .map(carrito -> {
                    carrito.add(linkTo(methodOn(CarritoController.class).obtenerUsuarioId(idUsuario)).withSelfRel());
                    carrito.add(linkTo(methodOn(CarritoController.class).obtenerTodo()).withRel("carritos"));
                    carrito.add(linkTo(methodOn(CarritoController.class).agregarItem(carrito.getIdCarrito(), new ItemCarritoRequestDTO())).withRel("agregar-item"));
                    return ResponseEntity.ok(carrito);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo carrito")
    public ResponseEntity<CarritoResponseDTO> guardarCarrito(@Valid @RequestBody CarritoRequestDTO dto) {
        CarritoResponseDTO nuevoCarrito = carritoService.guardar(dto);

        nuevoCarrito.add(linkTo(methodOn(CarritoController.class).obtenerUsuarioId(nuevoCarrito.getIdUsuario())).withSelfRel());
        nuevoCarrito.add(linkTo(methodOn(CarritoController.class).obtenerTodo()).withRel("carritos"));

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCarrito);
    }

    @PostMapping("/{idCarrito}/items")
    @Operation(summary = "Agregar item al carrito")
    public ResponseEntity<ItemCarritoResponseDTO> agregarItem(
            @PathVariable("idCarrito") Long idCarrito,
            @Valid @RequestBody ItemCarritoRequestDTO dto) {

        ItemCarritoResponseDTO nuevoItem = carritoService.agregarItemAlCarrito(idCarrito, dto);

        nuevoItem.add(linkTo(methodOn(CarritoController.class).agregarItem(idCarrito, new ItemCarritoRequestDTO())).withSelfRel());
        nuevoItem.add(linkTo(methodOn(CarritoController.class).actualizarCantidadItem(idCarrito, nuevoItem.getIdProducto(), nuevoItem.getCantidad())).withRel("actualizar-cantidad"));

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoItem);
    }

    @PutMapping("/{idCarrito}/items/{idProducto}")
    @Operation(summary = "Actualizar cantidad de un item")
    public ResponseEntity<ItemCarritoResponseDTO> actualizarCantidadItem(
            @PathVariable("idCarrito") Long idCarrito,
            @PathVariable("idProducto") Long idProducto,
            @RequestParam("cantidad") Integer cantidad) {

        ItemCarritoResponseDTO itemActualizado = carritoService.actualizarCantidadItem(idCarrito, idProducto, cantidad);

        itemActualizado.add(linkTo(methodOn(CarritoController.class).actualizarCantidadItem(idCarrito, idProducto, cantidad)).withSelfRel());
        itemActualizado.add(linkTo(methodOn(CarritoController.class).eliminarItem(idCarrito, idProducto)).withRel("eliminar"));

        return ResponseEntity.ok(itemActualizado);
    }

    @DeleteMapping("/{idCarrito}/items/{idProducto}")
    @Operation(summary = "Eliminar item del carrito")
    public ResponseEntity<Void> eliminarItem(
            @PathVariable("idCarrito") Long idCarrito,
            @PathVariable("idProducto") Long idProducto) {
        carritoService.eliminarItemDelCarrito(idCarrito, idProducto);
        return ResponseEntity.noContent().build();
    }
}