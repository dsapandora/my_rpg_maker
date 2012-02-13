#!/bin/sh
MYRPGMAKER="$(dirname "$0")"/myrpgmaker-library.jar
if [ -n "$JAVA_HOME" ]; then
  $JAVA_HOME/bin/java -jar "$MYRPGMAKER" --player gamedata "$@"
else
  java -jar "$MYRPGMAKER" --player gamedata "$@"
fi
