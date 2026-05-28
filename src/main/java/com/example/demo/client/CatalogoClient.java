package com.example.demo.client;

import com.example.demo.dto.ProductoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms_catalogo", url = "http://localhost:8082/api/producto")
public interface CatalogoClient {
    @GetMapping("/{id}")
    ProductoResponseDTO obtenerProductoPorId(@PathVariable("id") Long idProducto);

}
