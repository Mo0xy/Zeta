# BUILD
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copia i file POM (per sfruttare la cache Docker)
COPY pom.xml ./pom.xml
COPY common-lib/pom.xml ./common-lib/pom.xml
COPY user-service/pom.xml ./user-service/pom.xml
COPY pec-service/pom.xml ./pec-service/pom.xml

# Copia il codice sorgente
COPY common-lib/src ./common-lib/src
COPY user-service/src ./user-service/src
COPY pec-service/src ./pec-service/src

# Build del progetto
RUN mvn clean package -pl user-service -am -DskipTests

# RUNTIME
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crea utente non-root per sicurezza
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia il JAR dal build stage
COPY --from=build /workspace/user-service/target/*.jar app.jar

# Porta esposta
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Avvio applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
