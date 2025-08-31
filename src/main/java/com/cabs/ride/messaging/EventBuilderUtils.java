package com.cabs.ride.messaging;

import java.time.Instant;

public class EventBuilderUtils {
    private final static String CREATE_BY = "ride-service";

    public static <T> DomainEvent<T> build(DomainEventType eventType, T data) {
        return DomainEvent.<T>builder()
                .type(eventType.getType())
                .created(Instant.now().toString())
                .createdBy(CREATE_BY)
                .data(data)
                .build();
    }
}
