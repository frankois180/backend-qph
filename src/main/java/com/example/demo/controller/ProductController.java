package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "CRUD de productos incluido en el proyecto base. Solo el administrador.")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar productos")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id,
                                              @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
