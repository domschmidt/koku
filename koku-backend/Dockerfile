FROM openjdk:16-jdk-alpine
RUN apk --update add fontconfig ttf-dejavu
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]