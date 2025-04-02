FROM gradle:jdk23 AS build
COPY . /app/
RUN cd /app && gradle --build-cache assemble

FROM openjdk:23
RUN mkdir /app
COPY --from=build /app/build/libs/*.jar /app/KotlinBot.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/KotlinBot.jar"]
