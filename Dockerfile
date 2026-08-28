# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# Descarga dependencias primero (capa cacheada si pom.xml no cambia)
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S ecohome && adduser -S ecohome -G ecohome

COPY --from=build /build/target/ecohome-api-*.jar app.jar

USER ecohome
EXPOSE 8080

# UseContainerSupport: lee los límites de memoria del contenedor correctamente
# MaxRAMPercentage: usa máximo 75% de la RAM del contenedor para el heap JVM
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
