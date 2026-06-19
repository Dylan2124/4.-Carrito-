package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Representación de un carrito con enlaces HATEOAS")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarritoResponseDTO extends RepresentationModel<CarritoResponseDTO> {

    @Schema(description = "ID único del carrito"
            , example = "1"
    )
    private Long idCarrito; //
    @Schema(description = "ID del usuario propietario del carrito"
            , example = "42"
    )
    private Long idUsuario;
    @Schema(description = "Fecha y hora de creación del carrito"
            , example = "2026-06-18T12:34:56"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
    @Schema(description = "Lista de items en el carrito"
            , example = "[{\"idItem\":1,\"nombre\":\"Producto 1\",\"precio\":10.0,\"cantidad\":2}]"
    )
    private List<ItemCarritoResponseDTO> items;
}
