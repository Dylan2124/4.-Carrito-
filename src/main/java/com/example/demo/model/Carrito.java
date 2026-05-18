package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "carrito")
@Entity

public class Carrito {

    @Id
    @Column(name = "id_carrito")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    @Column(name = "id_usuario",nullable = false)
    private Long idUsuario;

    @Column(name = "fecha_creacion",nullable = false)
    private LocalDateTime fechaCreacion;
}
