package com.cabs.ride.messaging.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum RoutingKey {
    ROUTING_KEY_RIDE_STARTED("cabs.ride.customer.started"),
    ROUTING_KEY_RIDE_COMPLETED("cabs.ride.customer.completed");


    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static Optional<RoutingKey> from(String routingKey) {
        return Stream.of(values())
                .filter(e -> Objects.equals(e.getValue(), routingKey))
                .findFirst();
    }
}
