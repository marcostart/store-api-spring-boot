# Build stage
# Image Java légère
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/store-project-0.0.1-SNAPSHOT.jar app.jar

# Port Spring Boot
EXPOSE 9000

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]

