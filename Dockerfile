FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.3_7-jdk-jammy
COPY --from=build /target/cloud_media_gallery-0.0.1-SNAPSHOT.jar cloud_media_gallery.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","cloud_media_gallery.jar"]
