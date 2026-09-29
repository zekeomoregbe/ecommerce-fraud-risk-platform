FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends python3 && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/ecommerce-fraud-risk-platform-1.0.0.jar app.jar
COPY scripts ./scripts
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
