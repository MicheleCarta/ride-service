CREATE TABLE IF NOT EXISTS ride (
    id UUID PRIMARY KEY,
    driver_details_id UUID,
    ride_status VARCHAR,
    plate VARCHAR,
    pick_up VARCHAR,
    drop_off VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_driver_details_id
        FOREIGN KEY (driver_details_id)
        REFERENCES driver_details (id)
);
