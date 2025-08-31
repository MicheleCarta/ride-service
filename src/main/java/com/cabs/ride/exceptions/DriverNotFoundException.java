package com.cabs.ride.exceptions;

public class DriverNotFoundException extends RideException {
    public DriverNotFoundException() {
        super("Driver not found");
    }

    public DriverNotFoundException(String message) {
        super(message);
    }
} 