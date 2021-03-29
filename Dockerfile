FROM openjdk:8 AS builder
WORKDIR /usr/src/myapp
COPY . /usr/src/myapp
RUN ./gradlew -i war

FROM tomcat:10-jdk8
COPY --from=builder /usr/src/myapp/build/libs/COMP4321Project-1.0.war /usr/local/tomcat/webapps/ROOT.war
