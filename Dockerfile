FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y ffmpeg

WORKDIR /app

COPY target/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]