# syntax=docker/dockerfile:1.6

FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw .
# Ensure wrapper is executable and line endings are Unix
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -B -DskipTests dependency:go-offline

COPY src/ src/
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:23-jre-alpine-3.21
WORKDIR /opt/app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseStringDeduplication -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java","-jar","app.jar"]
