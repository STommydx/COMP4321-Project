#!/bin/bash

case $1 in

  startup)
    if [ ! -d ./tomcat ]
    then
      mkdir ./tomcat
      pushd ./tomcat || exit
      wget https://mirror-hk.koddos.net/apache/tomcat/tomcat-10/v10.0.5/bin/apache-tomcat-10.0.5.tar.gz
      tar xf apache-tomcat-10.0.5.tar.gz
      rm -r apache-tomcat-10.0.5/webapps/*
      popd || exit
    fi

    WAR_FILE=build/libs/COMP4321Project-1.0.war
    [ -f $WAR_FILE ] || ./gradlew war
    cp $WAR_FILE ./tomcat/apache-tomcat-10.0.5/webapps/ROOT.war

    env SE_DB_BASE_PATH="$PWD/db" ./tomcat/apache-tomcat-10.0.5/bin/startup.sh
    ;;

  shutdown)
    ./tomcat/apache-tomcat-10.0.5/bin/shutdown.sh
    ;;

  *)
    echo "Invalid command: $1"
    echo "Supported commands: startup, shutdown"
    ;;

esac