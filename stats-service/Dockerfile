FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

VOLUME /tmp

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY stats-server/target/stats-server-*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]