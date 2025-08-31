CREATE TABLE IF NOT EXISTS driver_details (
    id UUID PRIMARY KEY,
    vehicle_id UUID,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicle (id)
);
