FROM openjdk:17-jdk-slim

LABEL maintainer="spring-reactile-skill-rater@v1"

WORKDIR /app

COPY target/spring-reactive-skill-0.0.1-SNAPSHOT.jar  app.jar 

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]