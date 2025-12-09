FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

VOLUME /tmp

EXPOSE 8080

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY target/main-service-*.jar app.jar

COPY .github/workflows/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["sh", "-c", "/wait-for-it.sh main-db 5432 -- echo 'DB ready' && /wait-for-it.sh stats-service 9090 -- echo 'Stats ready' && java -jar app.jar"]