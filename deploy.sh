#!/usr/bin/env bash

#rm -rf build/
#echo "Deleted build/ folder"

#./gradlew bootJar
#echo "Generating jar file"

#Copy execute_commands_on_ec2.sh file which has commands to be executed on server... Here we are copying this file
# every time to automate this process through 'deploy.sh' so that whenever that file changes, it's taken care of
scp -i "~/.ssh/id_rsa" execute_commands_on_ec2.sh erik@64.225.72.235:/home/erik
echo "Copied latest 'execute_commands_on_ec2.sh' file from local machine to ec2 instance"

scp -i "~/.ssh/id_rsa" build/libs/secrethit.jar erik@64.225.72.235:/home/erik
echo "Copied jar file from local machine to ec2 instance"

echo "Connecting to ec2 instance and starting server using java -jar command"
ssh -i "~/.ssh/id_rsa" erik@64.225.72.235 ./execute_commands_on_ec2.sh
