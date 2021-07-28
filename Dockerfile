FROM openjdk:8-jdk-alpine

RUN mkdir -p /software

#ADD config /software/config

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /software/app.jar

# run the app
WORKDIR /software
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar /software/app.jar
