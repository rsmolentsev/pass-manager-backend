FROM gradle:8.6.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/src/main/resources/application.conf /app/application.conf
COPY --from=build /app/.env /app/.env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 