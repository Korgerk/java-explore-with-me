FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY stats-service/pom.xml stats-service/pom.xml
COPY stats-service/stats-dto/pom.xml stats-service/stats-dto/pom.xml
COPY stats-service/stats-client/pom.xml stats-service/stats-client/pom.xml
COPY stats-service/stats-server/pom.xml stats-service/stats-server/pom.xml
COPY main-service/pom.xml main-service/pom.xml

RUN mvn -DskipTests dependency:go-offline

COPY . .
RUN mvn -pl stats-service/stats-server -am -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/stats-service/stats-server/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
