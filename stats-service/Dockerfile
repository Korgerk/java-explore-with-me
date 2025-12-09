FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

VOLUME /tmp

EXPOSE 9090

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

RUN find . -name "*.jar" -type f | head -1 | xargs -I {} cp {} app.jar || (echo "JAR file not found!" && exit 1)

ENTRYPOINT ["sh", "-c", "echo 'Waiting for stats-db...' && \
  while ! nc -z stats-db 5432; do sleep 1; done && \
  echo 'Stats-db is ready, starting stats-service...' && \
  java \${JAVA_OPTS} -jar app.jar"]