FROM openjdk:21-jdk-slim
WORKDIR /app

COPY build/libs/ride-service-latest.jar .

ARG VERSION=latest
ENV APPLICATION_VERSION=$VERSION

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "ride-service-latest.jar"]

