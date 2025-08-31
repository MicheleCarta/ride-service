package com.cabs.ride.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.DirectRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Configuration(proxyBeanMethods = false)
@EnableRabbit
public class RabbitmqConfig {

    public static final String RideCustomer = "ride-customer";
    public static final String DLQ_SUFFIX = ".dlq";

    public static final String QUEUE_RIDE_STARTED = "cabs.ride.started";
    public static final String QUEUE_RIDE_COMPLETED = "cabs.ride.completed";

    @Bean
    TopicExchange domainEventsExchange() {
        TopicExchange topicExchange = new TopicExchange(RideCustomer);
        topicExchange.setIgnoreDeclarationExceptions(true);
        return topicExchange;
    }

    private List<Exchange> defaultExchanges() {
        return asList(domainEventsExchange());
    }

    @Bean
    public Queue rideStartedQueue() {
        return QueueBuilder.durable(QUEUE_RIDE_STARTED)
                .withArgument("x-dead-letter-exchange", RideCustomer)
                .withArgument("x-dead-letter-routing-key", QUEUE_RIDE_STARTED + DLQ_SUFFIX)
                .build();
    }

    @Bean
    public Queue rideStartedDlq() {
        return QueueBuilder.durable(QUEUE_RIDE_STARTED + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue rideCompletedQueue() {
        return QueueBuilder.durable(QUEUE_RIDE_COMPLETED)
                .withArgument("x-dead-letter-exchange", RideCustomer)
                .withArgument("x-dead-letter-routing-key", QUEUE_RIDE_COMPLETED + DLQ_SUFFIX)
                .build();
    }

    @Bean
    public Queue rideCompletedDlq() {
        return QueueBuilder.durable(QUEUE_RIDE_COMPLETED + DLQ_SUFFIX).build();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(RideCustomer);
        return template;
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary
    DirectRabbitListenerContainerFactory domainEventListenerContainerFactory(
            DirectRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            PlatformTransactionManager transactionManager
    ) {
        DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setTransactionManager(transactionManager);
        return factory;
    }

    @Bean
    public Binding rideStartedQueueBinding() {
        return BindingBuilder.bind(rideStartedQueue())
                .to(domainEventsExchange())
                .with(RoutingKey.ROUTING_KEY_RIDE_STARTED.getValue());
    }

    @Bean
    public Binding rideStartedDlqBinding() {
        return BindingBuilder.bind(rideStartedDlq())
                .to(domainEventsExchange())
                .with(QUEUE_RIDE_STARTED + DLQ_SUFFIX);
    }

    @Bean
    public Binding rideCompletedBinding() {
        return BindingBuilder.bind(rideCompletedQueue())
                .to(domainEventsExchange())
                .with(RoutingKey.ROUTING_KEY_RIDE_COMPLETED.getValue());
    }

    @Bean
    public Binding rideCompletedDlqBinding() {
        return BindingBuilder.bind(rideCompletedDlq())
                .to(domainEventsExchange())
                .with(QUEUE_RIDE_COMPLETED + DLQ_SUFFIX);
    }

    private Declarables declarables(Queue queue, List<Exchange> exchanges, List<RoutingKey> routingKeys) {
        return new Declarables(exchanges.stream()
                .flatMap(exchange -> routingKeys.stream()
                        .map(routingKey -> BindingBuilder.bind(queue)
                                .to(exchange)
                                .with(routingKey)
                                .noargs()))
                .collect(Collectors.toList()));
    }
}
