# =====================
# BUILD STAGE
# =====================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copiar el código fuente y empaquetar
COPY src ./src
RUN mvn -q clean package -DskipTests

# =====================
# RUNTIME STAGE
# =====================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el JAR generado desde la etapa de build
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Ajustes JVM para contenedores
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# Ejecutar como root para evitar problemas de usuario en Windows
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
