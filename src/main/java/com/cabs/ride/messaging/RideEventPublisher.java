package com.cabs.ride.messaging;

import com.cabs.ride.dto.RideCompleted;
import com.cabs.ride.dto.RideStarted;
import com.cabs.ride.messaging.config.RoutingKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RideEventPublisher {
    private final DomainEventPublisher publisher;

    public void publishRideStarted(RideStarted rideStarted) {
        DomainEvent<RideStarted> domainEvent = EventBuilderUtils.build(DomainEventType.STARTED, rideStarted);
        publisher.publishEventDomain(domainEvent, RoutingKey.ROUTING_KEY_RIDE_STARTED.getValue());
    }

    public void publishRideCompleted(RideCompleted rideCompleted) {
        DomainEvent<RideCompleted> domainEvent = EventBuilderUtils.build(DomainEventType.COMPLETED, rideCompleted);
        publisher.publishEventDomain(domainEvent, RoutingKey.ROUTING_KEY_RIDE_COMPLETED.getValue());
    }
}
