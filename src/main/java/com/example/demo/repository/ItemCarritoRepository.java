package com.example.demo.repository;

import com.example.demo.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByCarrito_IdCarrito(Long idCarrito);

    Optional<ItemCarrito> findByCarrito_IdCarritoAndIdProducto(Long idCarrito, Long idProducto);


}
