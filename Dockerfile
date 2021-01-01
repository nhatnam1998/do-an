FROM openjdk:11-jdk-slim

WORKDIR /app
COPY target/ .
ENTRYPOINT [ "java", "-jar", "e-commerce-0.0.1-SNAPSHOT.war" ]
