# Stage 1: Build the Spring Boot application using Maven and Temurin OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Ultra-lightweight Production JRE 17 Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JAR artifact from builder
COPY --from=build /app/target/ecommerce-java-1.0.0.jar app.jar

# Render assigns port dynamically via $PORT
EXPOSE 8081
ENV PORT=8081
ENV SPRING_PROFILES_ACTIVE=cloud

CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
