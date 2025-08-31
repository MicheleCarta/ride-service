package com.cabs.ride.repository;

import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverDetailsRepository extends JpaRepository<DriverDetails, UUID> {
    Optional<DriverDetails> findByVehicle(Vehicle vehicleId);
}
