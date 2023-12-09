FROM openjdk:17
WORKDIR /app
COPY target/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]