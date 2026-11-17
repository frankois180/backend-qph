package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateSaleRequest {

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    private List<@Valid SaleItemRequest> items;
}
