package com.example.demo.dto;

import lombok.Data;

@Data
public class ProductoResponseDTO {

    private Long idProducto;
    private String nombre;
    private Integer precioUnitario;
    private String fabricante;
    private String categoria;
}
