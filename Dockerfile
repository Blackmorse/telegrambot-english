FROM gradle:8.8.0-jdk21-jammy AS build

COPY ./ /home/gradle/app/
WORKDIR /home/gradle/app
RUN gradle build

FROM openjdk:21-slim
RUN mkdir /app
COPY --from=build /home/gradle/app/build/libs/telegrambot-english-0.0.1-SNAPSHOT-all.jar /app/bot.jar

ENTRYPOINT ["java", "-jar", "/app/bot.jar"]