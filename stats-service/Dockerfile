FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

VOLUME /tmp

EXPOSE 9090

RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY stats-server/target/stats-server-*.jar app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["/wait-for-it.sh", "stats-db", "5432", "--", "java", "-jar", "app.jar"]