package com.example.demo.assembler;

import com.example.demo.controller.CarritoController;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.model.Carrito;
import com.example.demo.model.ItemCarrito;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, CarritoResponseDTO> {

    /**     * Convierte una entidad Carrito a CarritoDTO e inyecta enlaces HATEOAS.     * Enlaces incluidos:     * - self: referencia al carrito mismo     * - vaciar: endpoint para vaciar el carrito     * - procesar-pago: endpoint siguiente en el flujo de negocio     */
    @Override
    public CarritoResponseDTO toModel(Carrito carrito) {
        CarritoResponseDTO dto = CarritoResponseDTO.builder()
                .idCarrito(carrito.getIdCarrito())
                .idUsuario(carrito.getIdUsuario())
                .fechaCreacion(carrito.getFechaCreacion())
                .items(carrito.getItems().stream()
                        .map(this::toItemDTO)
                        .collect(Collectors.toList()))
                .build();

        // Inyectar enlaces HATEOAS
        dto.add(linkTo(methodOn(CarritoController.class)
                .obtenerUsuarioId(carrito.getIdUsuario()))
                .withSelfRel()
                .withTitle("Ver carrito"));

        dto.add(linkTo(methodOn(CarritoController.class)
                .vaciarCarrito(carrito.getIdCarrito()))
                .withRel("vaciar")
                .withTitle("Vaciar carrito"));

        dto.add(linkTo(methodOn(CarritoController.class)
                .procesarPago(carrito.getIdCarrito()))
                .withRel("procesar-pago")
                .withTitle("Procesar pago"));

        return dto;
    }

    /**     * Convierte un ItemCarrito a ItemCarritoDTO.     */
    private ItemCarritoResponseDTO toItemDTO(ItemCarrito item) {
        return ItemCarritoResponseDTO.builder()
                .idItem(item.getIdItem())
                .idProducto(item.getIdProducto())
                .cantidad(item.getCantidad())
                .idCarrito(item.getCarrito().getIdCarrito())
                .build();
    }

    /**     * Convierte una colección de Carritos a CollectionModel con enlace self a la colección.     */
    @Override
    public CollectionModel<CarritoResponseDTO> toCollectionModel(Iterable<? extends Carrito> entities) {
        CollectionModel<CarritoResponseDTO> collectionModel =
                RepresentationModelAssembler.super.toCollectionModel(entities);

        collectionModel.add(linkTo(methodOn(CarritoController.class)
                .obtenerTodo())
                .withSelfRel()
                .withTitle("Todos los carritos"));

        return collectionModel;
    }
}
