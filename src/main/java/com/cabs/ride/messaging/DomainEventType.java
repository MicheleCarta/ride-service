package com.cabs.ride.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum DomainEventType {

    LEAD("Lead"),
    STARTED("Started"),
    COMPLETED("Completed"),
    UNKNOWN("unknown");

    @JsonValue
    private final String type;

    public static Optional<DomainEventType> from(String type) {
        return Stream.of(values())
                .filter(e -> Objects.equals(e.type, type))
                .findFirst();
    }
}
