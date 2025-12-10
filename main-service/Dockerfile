FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/main-service-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]