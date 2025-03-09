# Stage 1: Build
FROM gradle:8-jdk-21-and-22-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew clean bootJar -x test -x spotlessJavaCheck

# Stage 2: Runtime image
FROM eclipse-temurin:21-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*SNAPSHOT.jar spring.jar

EXPOSE 8080

# Start
ENTRYPOINT ["java", "-jar", "spring.jar"]
