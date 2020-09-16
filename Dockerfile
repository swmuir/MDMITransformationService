FROM maven:3-openjdk-15 AS builder
LABEL maintainer="Preston Lee <preston.lee@prestonlee.com>"
RUN mkdir /build
WORKDIR /build

COPY pom.xml pom.xml
RUN mvn dependency:go-offline

COPY src src
RUN mvn package

# RUN java -Xmx4g -jar input-cache/org.hl7.fhir.publisher.jar -ig .
# RUN ls -alh

FROM openjdk:8-jdk-alpine
LABEL maintainer="Preston Lee <preston.lee@prestonlee.com>"
# # We need to make a few changes to the default configuration file.
# COPY nginx.conf /etc/nginx/conf.d/default.conf
# # Copy faster-moving stuffinto the content directory.
RUN pwd
COPY --from=builder /build/target/org.mdmi.transformation.service.jar org.mdmi.transformation.service.jar
COPY maps maps
CMD exec java $JAVA_OPTS -jar org.mdmi.transformation.service.jar


# # Run the jar file 
# ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mdmi-transformation-service.jar"]

