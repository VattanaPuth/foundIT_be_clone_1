# Use an official OpenJDK image as a base
FROM openjdk:17-jdk-slim AS build

# Set the working directory
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

COPY --from=build /app/target/com-0.0.1-SNAPSHOT.jar .

# Expose the port your Spring Boot app runs on
EXPOSE 8085

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/com-0.0.1-SNAPSHOT.jar"]
