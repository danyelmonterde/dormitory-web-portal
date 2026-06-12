# Stage 1: Build Angular Frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend with Embedded Frontend
FROM maven:3.9.5-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
# Copy built Angular files into Spring Boot static resources directory
COPY --from=frontend-build /app/dist/frontend/browser ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Unified JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/dorm-portal-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
