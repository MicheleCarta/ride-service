package com.cabs.ride.messaging;

import com.cabs.ride.dto.RideCompleted;
import com.cabs.ride.dto.RideStarted;
import com.cabs.ride.messaging.config.RabbitmqConfig;
import com.cabs.ride.service.RideBookingSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DomainEventListener {
    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);

    private final RideBookingSupport rideBookingSupport;

    @RabbitListener(queues = RabbitmqConfig.QUEUE_RIDE_STARTED)
    public void handleRideStarted(@Payload DomainEvent<RideStarted> domainEvent) {
        try {
            log.info("Received Ride Started event {}", domainEvent);
            rideBookingSupport.startRide(domainEvent.getData());
        } catch (IllegalStateException e) {
            log.error("Error handling a Ride Started event {}", domainEvent, e);
        } catch (Exception e) {
            log.warn("Unexpected error handling a Ride Started event {}", domainEvent, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitmqConfig.QUEUE_RIDE_COMPLETED)
    public void handleRideCompleted(@Payload @Valid DomainEvent<RideCompleted> domainEvent) {
        try {
            log.info("Received Ride Completed event {}", domainEvent);
            rideBookingSupport.completeDriverRide(domainEvent.getData());
        } catch (IllegalStateException e) {
            log.error("Error handling a Ride Completed event {}", domainEvent, e);
        } catch (Exception e) {
            log.warn("Unexpected error handling a Ride Completed event {}", domainEvent, e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
