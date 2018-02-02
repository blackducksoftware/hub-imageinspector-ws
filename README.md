## Overview ##
A container-based Web Service for analyzing Docker images

# Build #
TBD

## Where can I get the latest release? ##
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-imageinspector-ws. 
You can download the latest release artifacts from GitHub: https://github.com/blackducksoftware/hub-imageinspector-ws/releases

## Documentation ##
hub-imageinspector-ws is under development.

src/main/resources/demo.sh is a shell script that get 3 containers running, and then suggests (echo's) some curl commands to test them.

Requirements: bash, docker, java 8, curl, port 8080, 8081, 8082, and a /tmp dir.

The script will start 3 imageinspectors. Each is a containerized web service. They'll run on ports 8080, 8081, and 8082. Each exposes a "getBdio" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns the list of components found in BDIO format (which can be uploaded to the Hub). 

You could send requests to any one of ports/containers and get the same result (assuming you follow redirects), but for simplicity you can send all requests to the same one (say, 8080). If the inspector you send the request to can't inspect the target image (because it doesn't have the right package manager), it redirects you to the one that can.

Supported package manager database formats: apk, dpkg (which apt also uses), and rpm (which yum also uses). 

