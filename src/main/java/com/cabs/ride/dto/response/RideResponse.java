package com.cabs.ride.dto.response;

import com.cabs.ride.model.RideStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RideResponse(UUID rideId, Double latitude, Double longitude, String plate, RideStatus rideStatus) {
}
