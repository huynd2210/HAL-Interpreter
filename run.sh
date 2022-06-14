#!/bin/bash
mvn package
cd target
echo "File to run"
read file
java -jar HALInterpreter-1.0-SNAPSHOT.jar $file