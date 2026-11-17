package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleItemRequest;
import com.example.demo.dto.SaleItemResponse;
import com.example.demo.dto.SaleResponse;
import com.example.demo.entity.Product;
import com.example.demo.entity.Sale;
import com.example.demo.entity.SaleItem;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Override
    public SaleResponse create(CreateSaleRequest request, String username) {
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Sale sale = Sale.builder()
                .user(seller)
                .createdAt(Instant.now())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : mergeQuantities(request).entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            if (productRepository.decrementStock(productId, quantity) == 0) {
                throw stockFailure(productId);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + productId));
            BigDecimal lineTotal = product.getPrecio().multiply(BigDecimal.valueOf(quantity));
            sale.addItem(SaleItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrecio())
                    .lineTotal(lineTotal)
                    .build());
            total = total.add(lineTotal);
        }

        sale.setTotal(total);
        return toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll(String username, boolean admin) {
        List<Sale> sales = admin
                ? saleRepository.findAllByOrderByCreatedAtDesc()
                : saleRepository.findByUser_UsernameOrderByCreatedAtDesc(username);
        return sales.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(Long id, String username, boolean admin) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        if (!admin && !sale.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("No tiene acceso a esta venta");
        }
        return toResponse(sale);
    }

    private Map<Long, Integer> mergeQuantities(CreateSaleRequest request) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (SaleItemRequest item : request.getItems()) {
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    private RuntimeException stockFailure(Long productId) {
        return productRepository.findById(productId)
                .map(product -> (RuntimeException) new InsufficientStockException(
                        "No hay stock suficiente para el producto " + product.getNombre()))
                .orElseGet(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + productId));
    }

    private SaleResponse toResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .username(sale.getUser().getUsername())
                .total(sale.getTotal())
                .createdAt(sale.getCreatedAt())
                .items(sale.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    private SaleItemResponse toItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getNombre())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
