FROM maven:3.8.4-openjdk-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package

FROM openjdk:17-alpine as RUN

COPY --from=build /app/target/secret_santa-1.0.jar .

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "secret_santa-1.0.jar" ]