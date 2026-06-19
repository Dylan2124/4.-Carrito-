package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Modelo para agregar un ítem al carrito")
public class ItemCarritoRequestDTO {
    @Schema(description = "ID del producto", example = "100")
    private Long idProducto;

    @Schema(description = "Cantidad del producto", example = "2")
    private Integer cantidad;
}
