FROM openjdk:11.0.6-jdk
RUN mkdir -p /maps
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
