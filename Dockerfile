FROM arm64v8/openjdk:11-jdk-slim

WORKDIR /app
COPY target/*.war app.war
ENTRYPOINT [ "java", "-jar", "app.war" ]
