FROM openjdk:13-slim
RUN mkdir /app
COPY build/libs/*.jar /app/KotlinBot.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/KotlinBot.jar"]
