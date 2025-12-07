FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:21.0.4_7-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/spring-boot-application.jar" ]
