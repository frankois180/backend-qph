package com.example.demo.messaging;

import com.example.demo.dto.InvoiceCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockKey;

    @Value("${app.rabbitmq.routing-key.notification}")
    private String notifKey;

    public void publishInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("Publishing InvoiceCreated event: invoiceId={}, productId={}",
                event.getInvoiceId(), event.getProductId());
        amqpTemplate.convertAndSend(exchange, stockKey, event);
        amqpTemplate.convertAndSend(exchange, notifKey, event);
    }
}
