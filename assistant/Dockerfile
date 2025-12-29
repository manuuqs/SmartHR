
# =====================
# BUILD STAGE
# =====================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# =====================
# RUNTIME STAGE
# =====================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/assistant-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 9090

# (Opcional) Healthcheck si usas Actuator:
# HEALTHCHECK --interval=30s --timeout=5s --retries=5 \
#   CMD wget -qO- http://localhost:9090/actuator/health | grep -q '"status":"UP"' || exit 1

RUN useradd -u 10002 appuser && chown -R appuser:appuser /app
USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
