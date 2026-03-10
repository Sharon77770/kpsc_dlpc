FROM gradle:8.7-jdk21 AS builder

WORKDIR /build
COPY . .
RUN gradle clean bootJar --no-daemon


FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y docker.io

COPY --from=builder /build/build/libs/*.jar app.jar
COPY src/main/resources/application.yml application.yml

EXPOSE 8000

ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.location=file:/app/application.yml"]