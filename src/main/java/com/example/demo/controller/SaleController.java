package com.example.demo.controller;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Venta de productos y consulta. El vendedor solo ve sus ventas.")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Registrar venta",
            description = "Relaciona usuario, productos y cantidades. Descuenta el stock. "
                    + "Responde 404 si el producto no existe y 409 si no hay stock suficiente.")
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody CreateSaleRequest request) {
        SaleResponse created = saleService.create(request, currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar ventas",
            description = "El administrador ve todas. El vendedor solo ve las suyas.")
    public ResponseEntity<List<SaleResponse>> getAll() {
        return ResponseEntity.ok(saleService.findAll(currentUsername(), isAdmin()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta",
            description = "Devuelve la venta si pertenece al usuario autenticado, o cualquiera si es administrador.")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id, currentUsername(), isAdmin()));
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
