# Stage 1: Build the Spring Boot application using Maven and Temurin OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy Maven project descriptor and source files
COPY pom.xml .
COPY src ./src

# Build production JAR package skipping test suite execution
RUN mvn clean package -Dmaven.test.skip=true

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
