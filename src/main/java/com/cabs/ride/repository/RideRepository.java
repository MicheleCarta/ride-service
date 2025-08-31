package com.cabs.ride.repository;

import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {
    Optional<Ride> findByDriver(DriverDetails driverDetails);
}
