FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S taskflow && adduser -S taskflow -G taskflow
USER taskflow

COPY --from=builder /build/target/taskflow-*.jar app.jar

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:3000/actuator/health 2>/dev/null || exit 1

EXPOSE 3000

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
