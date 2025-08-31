package com.cabs.ride.service;

import com.cabs.ride.dto.RideCompleted;
import com.cabs.ride.dto.RideStarted;
import com.cabs.ride.dto.request.PickupRequest;
import com.cabs.ride.dto.response.RideResponse;
import com.cabs.ride.exceptions.DriverNotFoundException;
import com.cabs.ride.exceptions.RideException;
import com.cabs.ride.messaging.RideEventPublisher;
import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Ride;
import com.cabs.ride.model.RideStatus;
import com.cabs.ride.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideBookingSupportTest {

    @Mock
    private DriverDetailsService driverDetailsService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private RideService rideService;

    @Mock
    private RideEventPublisher publisher;

    @InjectMocks
    private RideBookingSupport rideBookingSupport;

    private UUID testRideId;
    private PickupRequest testPickupRequest;
    private Vehicle testVehicle;
    private DriverDetails testDriverDetails;
    private Ride testRide;

    @BeforeEach
    void setUp() {
        testRideId = UUID.randomUUID();
        testPickupRequest = new PickupRequest(40.7128, -74.0060);
        testVehicle = new Vehicle();
        testVehicle.setPlate("ABC123");
        testVehicle.setLatitude(40.7128);
        testVehicle.setLongitude(-74.0060);
        testVehicle.setAvailable(true);

        testDriverDetails = new DriverDetails();
        testDriverDetails.setId(UUID.randomUUID());

        testRide = new Ride();
        testRide.setId(testRideId);
        testRide.setRideStatus(RideStatus.LEAD);
    }

    @Test
    void createRide_WhenDriversAvailable_ShouldCreateRideAndPublishEvent() {
        // Given
        when(vehicleService.hasAvailableDrivers()).thenReturn(true);
        when(rideService.saveOrUpdate(any(Ride.class))).thenReturn(testRide);

        // When
        RideResponse result = rideBookingSupport.createRide(testPickupRequest);

        // Then
        assertNotNull(result);
        assertEquals(testRideId, result.rideId());
        assertEquals(RideStatus.LEAD, result.rideStatus());
        assertEquals(testPickupRequest.latitude(), result.latitude());
        assertEquals(testPickupRequest.longitude(), result.longitude());

        // Verify
        verify(vehicleService).hasAvailableDrivers();
        verify(rideService).saveOrUpdate(any(Ride.class));
        verify(publisher).publishRideStarted(any(RideStarted.class));
    }

    @Test
    void createRide_WhenNoDriversAvailable_ShouldThrowDriverNotFoundException() {
        // Given
        when(vehicleService.hasAvailableDrivers()).thenReturn(false);

        // When & Then
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> rideBookingSupport.createRide(testPickupRequest));

        // Verify the exception message and inheritance
        assertEquals("Not Drivers Available at the moment", exception.getMessage());
        assertTrue(exception instanceof RideException);
        assertTrue(exception instanceof RuntimeException);

        // Verify that no further operations were performed
        verify(vehicleService).hasAvailableDrivers();
        verify(rideService, never()).saveOrUpdate(any(Ride.class));
        verify(publisher, never()).publishRideStarted(any(RideStarted.class));
    }

    @Test
    void startRide_WhenValidRideStarted_ShouldUpdateRideAndAllocateDriver() {
        // Given
        RideStarted rideStarted = RideStarted.builder()
                .rideId(testRideId)
                .latitude(40.7128)
                .longitude(-74.0060)
                .rideStatus(RideStatus.LEAD)
                .build();

        when(vehicleService.allocateNearestDriver(40.7128, -74.0060)).thenReturn(testVehicle);
        when(driverDetailsService.getDriverDetails(testVehicle)).thenReturn(testDriverDetails);
        when(rideService.getRide(testRideId)).thenReturn(testRide);
        when(rideService.saveOrUpdate(any(Ride.class))).thenReturn(testRide);

        // When
        rideBookingSupport.startRide(rideStarted);

        // Then
        assertEquals(RideStatus.STARTED, testRide.getRideStatus());
        assertEquals(testDriverDetails, testRide.getDriver());
        assertEquals("ABC123", testRide.getPlate());

        // Verify
        verify(vehicleService).allocateNearestDriver(40.7128, -74.0060);
        verify(driverDetailsService).getDriverDetails(testVehicle);
        verify(rideService).getRide(testRideId);
        verify(rideService).saveOrUpdate(testRide);
    }

    @Test
    void startRide_WhenNoVehiclesAvailable_ShouldThrowDriverNotFoundException() {
        // Given
        RideStarted rideStarted = RideStarted.builder()
                .rideId(testRideId)
                .latitude(40.7128)
                .longitude(-74.0060)
                .rideStatus(RideStatus.LEAD)
                .build();

        when(vehicleService.allocateNearestDriver(40.7128, -74.0060))
                .thenThrow(new DriverNotFoundException("Not Driver Available at the moment"));

        // When & Then
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> rideBookingSupport.startRide(rideStarted));

        // Verify the exception message and inheritance
        assertEquals("Not Driver Available at the moment", exception.getMessage());
        assertTrue(exception instanceof RideException);
        assertTrue(exception instanceof RuntimeException);

        // Verify that no further operations were performed after the exception
        verify(vehicleService).allocateNearestDriver(40.7128, -74.0060);
        verify(driverDetailsService, never()).getDriverDetails(any());
        verify(rideService, never()).getRide(any());
        verify(rideService, never()).saveOrUpdate(any(Ride.class));
    }

    @Test
    void completeRide_WhenValidRideId_ShouldPublishRideCompletedEvent() {
        // When
        rideBookingSupport.completeRide(testRideId);

        // Verify
        verify(publisher).publishRideCompleted(any(RideCompleted.class));
    }

    @Test
    void completeDriverRide_WhenValidRideCompleted_ShouldUpdateRideAndVehicle() {
        // Given
        RideCompleted rideCompleted = RideCompleted.builder()
                .rideId(testRideId)
                .build();

        testRide.setPlate("ABC123");
        when(rideService.getRide(testRideId)).thenReturn(testRide);
        when(vehicleService.getVehicleByPlate("ABC123")).thenReturn(testVehicle);
        when(rideService.saveOrUpdate(any(Ride.class))).thenReturn(testRide);

        // When
        rideBookingSupport.completeDriverRide(rideCompleted);

        // Then
        assertEquals(RideStatus.COMPLETED, testRide.getRideStatus());
        assertTrue(testVehicle.getAvailable());

        // Verify
        verify(rideService).getRide(testRideId);
        verify(vehicleService).getVehicleByPlate("ABC123");
        verify(rideService).saveOrUpdate(testRide);
        verify(vehicleService).saveOrUpdate(testVehicle);
    }

    @Test
    void saveRide_WhenRideCreation_ShouldSaveAndReturnRide() {
        // Given
        when(vehicleService.hasAvailableDrivers()).thenReturn(true);
        when(rideService.saveOrUpdate(any(Ride.class))).thenReturn(testRide);

        // When
        RideResponse result = rideBookingSupport.createRide(testPickupRequest);

        // Then
        assertNotNull(result);
        assertEquals(testRideId, result.rideId());
        assertEquals(RideStatus.LEAD, result.rideStatus());

        // Verify
        verify(rideService).saveOrUpdate(any(Ride.class));
    }
}
