# ==========================================
# Multi-stage Dockerfile for University Portal
# ==========================================

# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build

# Copy Maven wrapper and POM for layer caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Make wrapper script executable
RUN chmod +x mvnw

# Download dependencies offline (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy project source code
COPY src src

# Build and package the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Create lightweight runtime container
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Create a non-root user for security best practices
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy executable jar from builder stage
COPY --from=builder /build/target/university-portal-1.0.0.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose Spring Boot port
EXPOSE 8085

# Environment variables with sensible defaults
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
