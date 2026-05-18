package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCarritoResponseDTO {

    private Long idItem;
    private Long idCarrito;
    private Long idProducto;
    private Integer cantidad;
}
