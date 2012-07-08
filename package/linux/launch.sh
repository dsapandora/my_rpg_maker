#!/bin/sh
MYRPGMAKER="$(dirname "$0")"/editor-min.jar
if [ -n "$JAVA_HOME" ]; then
  $JAVA_HOME/bin/java -jar "$MYRPGMAKER" "$@"
else
  java -jar "$MYRPGMAKER" "$@"
fi
