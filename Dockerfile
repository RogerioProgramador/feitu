# ---- Stage 1: Build frontend ----
FROM node:20-slim AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: Build backend ----
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
# Pre-download dependencies (cache layer)
RUN mvn dependency:go-offline -P backend-only -q
COPY backend/src/ ./src/
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static/
RUN mvn package -P backend-only -DskipTests -B

# ---- Stage 3: Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /data

COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -Xmx160m -Xms32m -XX:+UseSerialGC -Xss256k -XX:MaxMetaspaceSize=128m -XX:MetaspaceSize=64m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
