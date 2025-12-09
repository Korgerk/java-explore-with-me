FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

VOLUME /tmp

EXPOSE 8080

ARG JAR_FILE=target/main-service-*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "echo 'Waiting for main-db and stats-service...' && \
  while ! nc -z main-db 5432; do sleep 1; done && \
  while ! nc -z stats-service 9090; do sleep 1; done && \
  echo 'All dependencies are ready, starting main-service...' && \
  java ${JAVA_OPTS} -jar app.jar"]