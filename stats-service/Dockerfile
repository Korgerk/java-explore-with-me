FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

VOLUME /tmp

ARG JAR_PATH=stats-server/target/*.jar
COPY ${JAR_PATH} app.jar

EXPOSE 9090

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]