FROM openjdk:17-jdk-slim

LABEL maintainer="spring-reactile-skill-rater@v1"

WORKDIR /app

COPY target/reactive-0.0.1-SNAPSHOT.jar  app.jar 

EXPOSE 8080

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","app.jar"]


# # Stage 1: Build the JAR file
# FROM maven:3.8.4-openjdk-17-slim AS build

# WORKDIR /app

# COPY pom.xml .
# COPY src ./src

# RUN mvn clean package -DskipTests

# # Stage 2: Create the final image
# FROM openjdk:17-jdk-slim

# LABEL maintainer="spring-reactile-skill-rater@v1"

# WORKDIR /app

# COPY --from=build /app/target/reactive-0.0.1-SNAPSHOT.jar app.jar 

# EXPOSE 8080

# ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
