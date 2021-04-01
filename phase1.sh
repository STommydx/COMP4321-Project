#!/bin/bash
JAR_FILE=build/libs/COMP4321Project-1.0-all.jar
[[ -f $JAR_FILE ]] || ./gradlew shadowJar
java -jar $JAR_FILE "$@"
