# --- Etapa 1: compilación con Maven + JDK 23 ---
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

# Copiamos POM y las fuentes
COPY pom.xml .
COPY src/ src/

# Construimos el JAR sin tests
RUN mvn clean package -DskipTests

# --- Etapa 2: ejecución con JRE 23 sobre Alpine Linux ---
FROM eclipse-temurin:23-jre-alpine-3.21
WORKDIR /opt/app

# Copiamos el JAR generado desde la etapa de build
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
