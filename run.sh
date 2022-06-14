#!/bin/bash
mvn package
cd target
echo "File to run"
read file
echo "Debug?(true/false)"
read debug
java -jar HALInterpreter-1.0-SNAPSHOT.jar $file $debug