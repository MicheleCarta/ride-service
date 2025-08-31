[![backend-service pipeline](https://github.com/invtd/backend/actions/workflows/cicd.yml/badge.svg)](https://github.com/invtd/backend/actions/workflows/cicd.yml)

# Ride Service

## Prerequisites

- Java 21
- Gradle
- Docker and Docker Compose
- PostgreSQL 12.8
- RabbitMQ

## Getting Started

### Swagger available

End-point information in Swagger http://service-url/doc/swagger-ui.html
End-point OpenAPI definition http://service-url/api-docs


### Build the Project

From the root directory, run:
```bash
./build.sh
```

### Run with Docker Compose

Start the required services:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- RabbitMQ on ports 5672 (AMQP) and 15672 (Management UI)

### Run the Application

From the root directory:
```bash
./gradlew bootRun
```

The application will start on port 3000.

## Service Endpoints

- Base URL: `http://localhost:3000`
- Actuator endpoints: `http://localhost:3000/actuator`

## Database Configuration

- Host: `localhost`
- Port: `5432`
- Database: `ride_db`
- Username: `ride`
- Password: `ride`

## RabbitMQ Configuration

- Host: `localhost`
- Port: `5672`
- Management UI: `http://localhost:15672`
- Default credentials: guest/guest

## Testing

Run tests with:
```bash
./gradlew test
```

Code coverage reports are generated using JaCoCo.
