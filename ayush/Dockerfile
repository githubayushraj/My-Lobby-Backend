# Use an official OpenJDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the executable jar file into the container
# Make sure the jar file name matches what Maven or Gradle produces
COPY target/*.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file when the container launches
ENTRYPOINT ["java", "-jar", "app.jar"]