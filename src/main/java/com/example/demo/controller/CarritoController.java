package com.example.demo.controller;

import com.example.demo.dto.CarritoRequestDTO;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> obtenerTodo(){
        return ResponseEntity.ok(carritoService.obtenerTodos());
    }


    @GetMapping("/{idUsuario}")
    public ResponseEntity<CarritoResponseDTO> obtenerUsuarioId(@PathVariable Long idUsuario){
        return carritoService.obtenerPorUsuarioId(idUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarritoResponseDTO> guardarCarrito(@Valid @RequestBody CarritoRequestDTO dto){
        CarritoResponseDTO nuevoCarrito = carritoService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCarrito);
    }

    @PutMapping("/{idCarrito}/items/{idProducto}")
    public ResponseEntity<ItemCarritoResponseDTO> actualizarCantidadItem(@PathVariable("idCarrito") Long idCarrito,
            @PathVariable("idProducto") Long idProducto,
            @RequestParam("cantidad") Integer cantidad) {

        ItemCarritoResponseDTO itemActualizado = carritoService.actualizarCantidadItem(idCarrito, idProducto, cantidad);
        return ResponseEntity.ok(itemActualizado);
    }

    @DeleteMapping("/{idCarrito}/items/{idProducto}")
    public ResponseEntity<Void> eliminarItem(@PathVariable("idCarrito") Long idCarrito, @PathVariable("idProducto") Long idProducto) {
        carritoService.eliminarItemDelCarrito(idCarrito, idProducto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idCarrito}/items")
    public ResponseEntity<ItemCarritoResponseDTO> agregarItem(@PathVariable("idCarrito") Long idCarrito, @Valid @RequestBody ItemCarritoRequestDTO dto){
        ItemCarritoResponseDTO nuevoItem = carritoService.agregarItemAlCarrito(idCarrito, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoItem);
    }



}
