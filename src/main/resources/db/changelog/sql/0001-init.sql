create table if not exists vehicle
(
    id UUID primary key,
    longitude   double precision not null,
    latitude    double precision not null,
    available   boolean   not null default false,
    plate   varchar not null unique,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
