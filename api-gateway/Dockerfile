# Build context is the repository root (see docker-compose.yml).
FROM gradle:9.8.0-jdk25 AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY data-model data-model
COPY api-gateway api-gateway
RUN ./gradlew :api-gateway:bootJar --no-daemon -q

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/build/libs/api-gateway-*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
