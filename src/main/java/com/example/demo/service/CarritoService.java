package com.example.demo.service;

import com.example.demo.dto.CarritoRequestDTO;
import com.example.demo.dto.CarritoResponseDTO;
import com.example.demo.dto.ItemCarritoRequestDTO;
import com.example.demo.dto.ItemCarritoResponseDTO;
import com.example.demo.model.Carrito;
import com.example.demo.model.ItemCarrito;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;

    private final ItemCarritoRepository itemCarritoRepository;

    private ItemCarritoResponseDTO mapItemToDTO(ItemCarrito item){
        return new ItemCarritoResponseDTO(
                item.getIdItem(),
                item.getIdCarrito(),
                item.getIdProducto(),
                item.getCantidad()
        );
    }

    private CarritoResponseDTO mapToCarritoDTO(Carrito carrito){
        List<ItemCarritoResponseDTO> itemsDTO = itemCarritoRepository.findByIdCarrito(carrito.getIdCarrito())
                .stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

            return new CarritoResponseDTO(
                    carrito.getIdCarrito(),
                    carrito.getIdUsuario(),
                    carrito.getFechaCreacion(),
                    itemsDTO

            );
    }

    public List<CarritoResponseDTO> obtenerTodos() {
        return carritoRepository.findAll()
                .stream()
                .map(this::mapToCarritoDTO)
                .collect(Collectors.toList());
    }


    public Optional<CarritoResponseDTO> obtenerPorUsuarioId(Long idUsuario) {
        return carritoRepository.findByIdUsuario(idUsuario)
                .map(this::mapToCarritoDTO);
    }

    public CarritoResponseDTO guardar(CarritoRequestDTO dto) {
        Carrito carrito = new Carrito();
        carrito.setIdUsuario(dto.getIdUsuario());
        carrito.setFechaCreacion(LocalDateTime.now());

        Carrito guardado = carritoRepository.save(carrito);
        return mapToCarritoDTO(guardado);
    }

    public ItemCarritoResponseDTO actualizarCantidadItem(Long idCarrito, Long idProducto, Integer nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        ItemCarrito item = itemCarritoRepository.findByIdCarritoAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() -> new RuntimeException("El producto no existe en este carrito"));
        item.setCantidad(nuevaCantidad);
        ItemCarrito actualizado = itemCarritoRepository.save(item);
        return mapItemToDTO(actualizado);
    }

    public void eliminarItemDelCarrito(Long idCarrito, Long idProducto) {
        ItemCarrito item = itemCarritoRepository.findByIdCarritoAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() -> new RuntimeException("El producto no existe en este carrito"));

        itemCarritoRepository.delete(item);
    }

    public ItemCarritoResponseDTO agregarItemAlCarrito(Long idCarrito, ItemCarritoRequestDTO dto) {
        Carrito carrito = carritoRepository.findById(idCarrito)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + idCarrito));

        Optional<ItemCarrito> itemExistente = itemCarritoRepository.findByIdCarritoAndIdProducto(idCarrito, dto.getIdProducto());

        ItemCarrito item;
        if (itemExistente.isPresent()) {
            item = itemExistente.get();
            item.setCantidad(item.getCantidad() + dto.getCantidad());
        } else {
            item = new ItemCarrito();
            item.setIdCarrito(idCarrito);
            item.setIdProducto(dto.getIdProducto());
            item.setCantidad(dto.getCantidad());
        }

        ItemCarrito guardado = itemCarritoRepository.save(item);
        return mapItemToDTO(guardado);
    }
}