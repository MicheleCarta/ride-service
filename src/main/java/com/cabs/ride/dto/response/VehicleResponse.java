package com.cabs.ride.dto.response;

import lombok.Builder;

@Builder
public record VehicleResponse(
        String plate,
        Double longitude,
        Double latitude
) {
    public RideResponse toPickupResponse() {
        return RideResponse.builder()
                .plate(plate)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }
}
