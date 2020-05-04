FROM openjdk:13-jre-slim
RUN mkdir /app
COPY build/libs/*.jar /app/SpringBootBot.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/SpringBootBot.jar"]
