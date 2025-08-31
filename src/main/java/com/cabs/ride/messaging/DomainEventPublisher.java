package com.cabs.ride.messaging;

import com.cabs.ride.messaging.config.RabbitmqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEventDomain(DomainEvent data, String rk) {
        log.info("Publish [{}] event for Ride [{}] ", data.getType(), rk);
        rabbitTemplate.convertAndSend(
                RabbitmqConfig.RideCustomer,
                rk,
                data);
    }
}
