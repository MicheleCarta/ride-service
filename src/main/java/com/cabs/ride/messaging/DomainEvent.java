package com.cabs.ride.messaging;

import lombok.*;

@RequiredArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class DomainEvent<T> {
    private final String type;
    private final String created;
    private final String createdBy;
    private final T data;
}
