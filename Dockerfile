FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

RUN mvn clean install -pl app -am -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /workspace/app/target/app.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]