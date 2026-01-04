FROM --platform=linux/amd64 gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test

FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]