FROM openjdk:8 AS cache
WORKDIR /usr/src/myapp
COPY gradle /usr/src/myapp/gradle/
COPY build.gradle gradlew /usr/src/myapp/
RUN ./gradlew -i clean build

FROM openjdk:8 AS builder
COPY --from=cache /root/.gradle /root/.gradle
WORKDIR /usr/src/myapp
COPY . /usr/src/myapp
RUN ./gradlew -i war

FROM tomcat:10-jdk8
COPY --from=builder /usr/src/myapp/build/libs/COMP4321Project-1.0.war /usr/local/tomcat/webapps/ROOT.war
