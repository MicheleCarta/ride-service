package com.cabs.ride.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RideCompleted(@NotNull UUID rideId) {
}
