package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.stock}")
    private String stockQueue;

    @Value("${app.rabbitmq.queue.notification}")
    private String notifQueue;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockKey;

    @Value("${app.rabbitmq.routing-key.notification}")
    private String notifKey;

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(stockQueue, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notifQueue, true);
    }

    @Bean
    public Binding stockBinding(Queue stockQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(stockQueue).to(invoiceExchange).with(stockKey);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(notificationQueue).to(invoiceExchange).with(notifKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
