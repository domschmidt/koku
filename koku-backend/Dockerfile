FROM openjdk:17-alpine

ENV TZ 'Europe/Berlin'

RUN apk --update add fontconfig ttf-dejavu tzdata
RUN cp /usr/share/zoneinfo/${TZ} /etc/localtime
RUN rm -rf /var/cache/apk/*

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
