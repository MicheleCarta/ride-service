package com.cabs.ride.dto.request;

import jakarta.validation.constraints.NotNull;

public record PickupRequest(@NotNull Double latitude, @NotNull Double longitude) {
}
