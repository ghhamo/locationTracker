# Use an official OpenJDK image as a base
FROM eclipse-temurin:23-jdk

# Set the working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY build/libs/userService-*.jar user-service.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "user-service.jar"]
