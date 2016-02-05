#!/usr/bin/env bash
MVN=mvn
JAVA=java

BIG_BASH_JAR=$(find . -name bigbash*-jar-with-dependencies.jar)
if [[ ! -e "$BIG_BASH_JAR" ]]; then
    $MVN clean install assembly:single
    BIG_BASH_JAR=$(find . -name bigbash*-jar-with-dependencies.jar)
    if [[ ! -e "$BIG_BASH_JAR" ]]; then
        echo "Something went wrong! Please check the error messages above."
        exit 100
    fi
fi

java -jar $BIG_BASH_JAR "$@"
