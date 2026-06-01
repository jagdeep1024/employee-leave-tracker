package com.assignment.leave;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "leave.events";
    public static final String QUEUE = "notification.events";
    public static final String ROUTING_KEY = "leave.notification";

    @Bean
    DirectExchange leaveExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, DirectExchange leaveExchange) {
        return BindingBuilder.bind(notificationQueue).to(leaveExchange).with(ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
