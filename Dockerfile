FROM gradle:8.10.1-jdk21 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN --mount=type=secret,id=github_token,env=GITHUB_TOKEN,required \
    --mount=type=secret,id=github_user,env=GITHUB_ACTOR,required \
    gradle assemble
FROM eclipse-temurin:21.0.4_7-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/spring-boot-application.jar" ]
