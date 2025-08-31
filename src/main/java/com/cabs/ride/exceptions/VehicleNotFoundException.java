package com.cabs.ride.exceptions;

public class VehicleNotFoundException extends RideException {
    public VehicleNotFoundException() {
        super("Vehicle not found");
    }

    public VehicleNotFoundException(String message) {
        super(message);
    }
} 