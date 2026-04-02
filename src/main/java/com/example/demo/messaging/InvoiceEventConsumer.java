package com.example.demo.messaging;

import com.example.demo.dto.InvoiceCreatedEvent;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventConsumer {

    private final ProductRepository productRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue.stock}")
    @Transactional
    public void handleStockUpdate(InvoiceCreatedEvent event) {
        log.info("[STOCK CONSUMER] Procesando evento para producto id={}", event.getProductId());
        productRepository.findById(event.getProductId()).ifPresentOrElse(
            product -> {
                int newStock = Math.max(0, product.getStock() - event.getQuantity());
                product.setStock(newStock);
                productRepository.save(product);
                log.info("[STOCK CONSUMER] Stock actualizado: producto='{}', nuevoStock={}",
                        product.getNombre(), newStock);
            },
            () -> log.warn("[STOCK CONSUMER] Producto no encontrado con id={}", event.getProductId())
        );
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.notification}")
    public void handleNotification(InvoiceCreatedEvent event) {
        BigDecimal total = event.getUnitPrice().multiply(BigDecimal.valueOf(event.getQuantity()));
        log.info("[NOTIFICATION CONSUMER] Email simulado — Factura: {}, Producto: {}, Total: ${}",
                event.getInvoiceId(), event.getProductName(), total);
    }
}
