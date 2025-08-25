FROM openjdk:21-jdk-slim

WORKDIR /app

COPY app/target/app.jar app.jar

CMD ["java", "-jar", "app.jar"]