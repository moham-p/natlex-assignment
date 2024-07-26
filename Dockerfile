FROM gradle AS build

WORKDIR /home/gradle/project

COPY . .

RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=dev"]
