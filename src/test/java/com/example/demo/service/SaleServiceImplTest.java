package com.example.demo.service;

import com.example.demo.dto.CreateSaleRequest;
import com.example.demo.dto.SaleItemRequest;
import com.example.demo.entity.Product;
import com.example.demo.entity.Sale;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    @Test
    void rejectsSaleWhenStockIsInsufficient() {
        when(userRepository.findByUsername("vendedor")).thenReturn(Optional.of(seller()));
        when(productRepository.decrementStock(1L, 3)).thenReturn(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, "Teclado", 2)));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> saleService.create(request(1L, 3), "vendedor"));

        assertEquals("No hay stock suficiente para el producto Teclado", exception.getMessage());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void rejectsSaleWhenProductDoesNotExist() {
        when(userRepository.findByUsername("vendedor")).thenReturn(Optional.of(seller()));
        when(productRepository.decrementStock(9L, 1)).thenReturn(0);
        when(productRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> saleService.create(request(9L, 1), "vendedor"));
        verify(saleRepository, never()).save(any());
    }

    @Test
    void sellerCannotReadAnotherUsersSale() {
        when(saleRepository.findById(4L)).thenReturn(Optional.of(Sale.builder()
                .id(4L)
                .user(User.builder().id(1L).username("otro").role(UserRole.VENDEDOR).build())
                .total(BigDecimal.TEN)
                .createdAt(Instant.parse("2026-11-17T12:00:00Z"))
                .build()));

        assertThrows(ForbiddenException.class, () -> saleService.findById(4L, "vendedor", false));
    }

    private CreateSaleRequest request(Long productId, int quantity) {
        SaleItemRequest item = new SaleItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item));
        return request;
    }

    private User seller() {
        return User.builder().id(2L).username("vendedor").role(UserRole.VENDEDOR).build();
    }

    private Product product(Long id, String nombre, int stock) {
        return Product.builder()
                .id(id)
                .nombre(nombre)
                .precio(new BigDecimal("10.00"))
                .stock(stock)
                .build();
    }
}
