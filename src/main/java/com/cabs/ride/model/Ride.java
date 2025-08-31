package com.cabs.ride.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ride")
@Getter
@Setter
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_details_id")
    private DriverDetails driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_status")
    private RideStatus rideStatus;

    private String plate;
    @Column(name = "pick_up")
    private String pickup;
    @Column(name = "drop_off")
    private String dropOff;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
