FROM gradle:8.10.1-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
# Args para credenciales
ARG GPR_USER
ARG GPR_KEY
#exponemos como env para que Gradle las lea
ENV GPR_USER=${GPR_USER}
ENV GPR_KEY=${GPR_KEY}

RUN gradle --no-daemon assemble

FROM eclipse-temurin:21.0.4_7-jre
EXPOSE 8080
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=production","/app/app.jar"]
