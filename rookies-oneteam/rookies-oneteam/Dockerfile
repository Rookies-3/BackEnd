# Use OpenJDK 17 for running the pre-built JAR
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the pre-built JAR file
COPY build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
