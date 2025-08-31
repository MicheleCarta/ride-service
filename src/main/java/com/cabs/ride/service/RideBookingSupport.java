package com.cabs.ride.service;

import com.cabs.ride.dto.RideCompleted;
import com.cabs.ride.dto.RideCreation;
import com.cabs.ride.dto.RideStarted;
import com.cabs.ride.dto.request.PickupRequest;
import com.cabs.ride.dto.response.RideResponse;
import com.cabs.ride.exceptions.DriverNotFoundException;
import com.cabs.ride.messaging.RideEventPublisher;
import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Ride;
import com.cabs.ride.model.RideStatus;
import com.cabs.ride.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RideBookingSupport {
    private static final Logger logger = LoggerFactory.getLogger(RideBookingSupport.class);

    private final DriverDetailsService driverDetailsService;
    private final VehicleService vehicleService;
    private final RideService rideService;
    private final RideEventPublisher publisher;


    public RideResponse createRide(PickupRequest pickupRequest) {
        if (!vehicleService.hasAvailableDrivers()) {
            throw new DriverNotFoundException("Not Drivers Available at the moment");
        }
        RideCreation ride = RideCreation.builder().rideStatus(RideStatus.LEAD).latitude(pickupRequest.latitude()).longitude(pickupRequest.longitude()).build();
        Ride rideLead = saveRide(ride);
        RideStarted rideStarted = RideStarted.builder().rideStatus(rideLead.getRideStatus())
                .rideId(rideLead.getId())
                .latitude(pickupRequest.latitude())
                .longitude(pickupRequest.longitude())
                .build();
        publisher.publishRideStarted(rideStarted);

        return RideResponse.builder()
                .longitude(pickupRequest.longitude())
                .latitude(pickupRequest.latitude())
                .rideStatus(rideLead.getRideStatus())
                .rideId(rideLead.getId())
                .build();
    }

    public RideResponse getCurrentRide(UUID rideId) {
        Ride ride = rideService.getRide(rideId);
        return RideResponse.builder().plate(ride.getPlate())
                .latitude(Double.valueOf(ride.getPickup().split(",")[0]))
                .longitude(Double.valueOf(ride.getPickup().split(",")[1]))
                .rideStatus(ride.getRideStatus())
                .rideId(rideId)
                .build();
    }


    public void startRide(RideStarted rideStarted) {
        Vehicle vehicle = vehicleService.allocateNearestDriver(rideStarted.latitude(), rideStarted.longitude());
        DriverDetails driverDetails = driverDetailsService.getDriverDetails(vehicle);
        Ride ride = rideService.getRide(rideStarted.rideId());
        ride.setRideStatus(RideStatus.STARTED);
        ride.setDriver(driverDetails);
        ride.setPlate(vehicle.getPlate());
        rideService.saveOrUpdate(ride);
        logger.info("Ride started for {}", ride.getId());
    }

    public void completeRide(UUID rideId) {
        publisher.publishRideCompleted(RideCompleted.builder().rideId(rideId).build());
    }

    public void completeDriverRide(RideCompleted rideCompleted) {
        Ride ride = rideService.getRide(rideCompleted.rideId());
        Vehicle vehicle = vehicleService.getVehicleByPlate(ride.getPlate());

        ride.setRideStatus(RideStatus.COMPLETED);
        rideService.saveOrUpdate(ride);

        vehicle.setAvailable(true);
        vehicleService.saveOrUpdate(vehicle);
        logger.info("Ride completed for {}", ride.getId());
    }

    private Ride saveRide(RideCreation ride) {
        Ride rideLead = new Ride();
        rideLead.setRideStatus(RideStatus.LEAD);
        rideLead.setDriver(ride.driver());
        rideLead.setPickup(ride.latitude()+","+ride.longitude());
        return rideService.saveOrUpdate(rideLead);
    }

}
