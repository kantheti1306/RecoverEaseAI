# ===== Stage 1: Build =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Stage 2: Run =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S recoverease && adduser -S recoverease -G recoverease
COPY --from=build /app/target/*.jar app.jar
RUN chown recoverease:recoverease app.jar
USER recoverease
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
