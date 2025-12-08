FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

VOLUME /tmp

ARG JAR_FILE=target/*.jar
COPY target/stats-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]