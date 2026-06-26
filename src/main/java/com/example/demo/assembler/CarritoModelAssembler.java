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

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.stream.StreamSupport;
@Component
@RequiredArgsConstructor
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, CarritoResponseDTO> {

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

        dto.add(linkTo(methodOn(CarritoController.class)
                .obtenerUsuarioId(carrito.getIdUsuario()))
                .withSelfRel());

        return dto;
    }

    private ItemCarritoResponseDTO toItemDTO(ItemCarrito item) {
        return ItemCarritoResponseDTO.builder()
                .idItem(item.getIdItem())
                .idProducto(item.getIdProducto())
                .cantidad(item.getCantidad())
                .idCarrito(item.getCarrito() != null ? item.getCarrito().getIdCarrito() : null)
                .build();
    }

    @Override
    public CollectionModel<CarritoResponseDTO> toCollectionModel(Iterable<? extends Carrito> entities) {
        List<CarritoResponseDTO> list = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<CarritoResponseDTO> collectionModel = CollectionModel.of(list);

        collectionModel.add(linkTo(methodOn(CarritoController.class)
                .obtenerTodo())
                .withSelfRel());

        return collectionModel;
    }
}