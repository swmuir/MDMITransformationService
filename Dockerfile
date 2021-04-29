FROM maven:3.6.3-openjdk-15 AS builder
LABEL maintainer="Preston Lee <preston.lee@prestonlee.com>"
RUN mkdir /build
WORKDIR /build

COPY pom.xml pom.xml
##RUN mvn dependency:go-offline

COPY src src
RUN mvn -U package

FROM openjdk:11.0.6-jdk

RUN pwd
COPY --from=builder /build/target/org.mdmi.transformation.service.jar org.mdmi.transformation.service.jar
COPY maps maps
CMD exec java $JAVA_OPTS -jar org.mdmi.transformation.service.jar


# # Run the jar file
# ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mdmi-transformation-service.jar"]
