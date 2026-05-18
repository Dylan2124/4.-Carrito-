package com.example.demo.repository;

import com.example.demo.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito,Long> {

    Optional<Carrito> findByIdUsuario(Long idUsuario);


}
