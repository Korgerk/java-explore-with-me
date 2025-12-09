FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

VOLUME /tmp

COPY target/main-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]