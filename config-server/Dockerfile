FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Install git and curl for config checkout and healthchecks
RUN apk add --no-cache git curl
COPY --from=builder /app/target/*.jar app.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
EXPOSE 8888
ENTRYPOINT ["./entrypoint.sh"]
