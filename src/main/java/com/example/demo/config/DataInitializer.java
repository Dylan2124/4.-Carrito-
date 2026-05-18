package com.example.demo.config;

import com.example.demo.model.Carrito;
import com.example.demo.model.ItemCarrito;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;

    @Override
    public void run(String... args) {
        if (carritoRepository.count() > 0) {
            log.info(">>> Base de datos de carritos ya tiene datos. Omitiendo inicializacion");
            return;
        }

        log.info(">>> Cargando carritos iniciales asociados a los IDs de usuarios del MS Usuarios.");

        Carrito carrito1 = new Carrito();
        carrito1.setIdUsuario(6L);
        carrito1.setFechaCreacion(LocalDateTime.now());
        Carrito carritoGuardado = carritoRepository.save(carrito1);

        // 2. Agregar ítems a este carrito
        ItemCarrito item1 = new ItemCarrito();
        item1.setIdCarrito(carritoGuardado.getIdCarrito());
        item1.setIdProducto(1L);
        item1.setCantidad(2);
        itemCarritoRepository.save(item1);

        ItemCarrito item2 = new ItemCarrito();
        item2.setIdCarrito(carritoGuardado.getIdCarrito());
        item2.setIdProducto(3L);
        item2.setCantidad(1);
        itemCarritoRepository.save(item2);


        log.info(">>> Carritos e ítems cargados exitosamente.");
    }
}
