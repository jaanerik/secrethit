#!/usr/bin/env bash

kill -9 $(lsof -t -i:8080)
echo "Killed process running on port 8080"

java -jar secrethit.jar
echo "Started server using java -jar command"
