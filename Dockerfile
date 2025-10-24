# syntax=docker/dockerfile:1

FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# Credenciales para resolver dependencias privadas (solo en build)
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}
ENV GPR_USER=${GITHUB_ACTOR}
ENV GPR_KEY=${GITHUB_TOKEN}

COPY . .
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=docker"]
