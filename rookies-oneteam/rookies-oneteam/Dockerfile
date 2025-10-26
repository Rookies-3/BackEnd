# Multi-stage build for Spring Boot application
FROM gradle:8-jdk17-alpine AS build

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy all source files
COPY src ./src

# Build the application
RUN gradle build -x test

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
