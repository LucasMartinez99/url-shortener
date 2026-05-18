# ─── Stage 1: Build ───────────────────────────────────────────────────────────
# Use the official Maven image so we don't need a local mvn install
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first and download dependencies (cached unless pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
# JRE-only image — smaller than JDK, no compiler needed at runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root user: best practice for container security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
