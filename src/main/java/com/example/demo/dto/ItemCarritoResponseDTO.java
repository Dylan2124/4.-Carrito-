package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Detalle de un ítem contenido en el carrito")
public class ItemCarritoResponseDTO extends RepresentationModel<ItemCarritoResponseDTO> {

    @Schema(description = "ID único del item"
            , example = "10"
    )
    private Long idItem;

    @Schema(description = "ID del producto (referencia a microservicio de catálogo)"
            , example = "100"
    )
    private Long idCarrito;

    @Schema(description = "Cantidad de unidades seleccionadas del producto"
            , example = "2"
    )
    private Long idProducto;

    @Schema(description = "ID del carrito propietario"
            , example = "1"
    )
    private Integer cantidad;
}
