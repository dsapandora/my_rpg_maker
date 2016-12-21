#!/bin/bash
INPUT_STRING=hello
while [ "$INPUT_STRING" != "bye" ]
do
  file=`ls -rtR -d $PWD/desktop/src/main/* |  shuf -n1`
  echo "Please type something in (bye to quit) $file".
  read INPUT_STRING
  echo "You typed: $INPUT_STRING"
  fecha=`date -d "$((RANDOM%1+2012))-$((RANDOM%12+1))-$((RANDOM%28+1))T$((RANDOM%23+1)):$((RANDOM%59+1)):$((RANDOM%59+1))" '+%Y-%m-%dT%H:%M:%S'`
  git add "$file" 
  export GIT_AUTHOR_DATE=$fecha
  export GIT_COMMITTER_DATE=$fecha
  git commit -m "$INPUT_STRING"
done
