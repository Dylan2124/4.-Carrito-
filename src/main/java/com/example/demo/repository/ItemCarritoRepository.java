package com.example.demo.repository;

import com.example.demo.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByIdCarrito(Long idCarrito);

    Optional<ItemCarrito> findByIdCarritoAndIdProducto(Long idCarrito, Long idProducto);
}
