## Overview ##
A container-based Web Service for analyzing Docker images

# Build #
TBD

## Where can I get the latest release? ##
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-imageinspector-ws. 

There are no releases yet, but eventually:
You can download the latest release artifacts from GitHub: https://github.com/blackducksoftware/hub-imageinspector-ws/releases

## Documentation ##
hub-imageinspector-ws is under development. You can use provided bash scripts to try a pre-release version in either a Kubernetes or a Docker environment.

You only need files in the src/main/resources directory, but it's probably easiest to clone the whole repo. For the relative paths to be correct, execute the scripts (src/main/resources/demo-*.sh) from the top level directory (the one that contains build.gradle). Whichever script you use, you'll want to read the script to understand what it's doing.

### Trying hub-imageinspector-ws in a Kubernetes (minikube) environment ##

src/main/resources/demo-minikube.sh is a shell script that uses minikube to get a pod running, and then executes (and echo's) some curl commands to test the service.

Requirements: bash, minikube, java 8, curl, port 8080, 8081, 8082. It creates a ~/tmp/target dir.

The script will start a 3-container pod, and expose ports 8080, 8081, and 8082. Each port has a "getBdio" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns the list of components found in BDIO format (which can be uploaded to the Hub). 

You could send requests to any one of ports and get the same result (assuming you follow redirects), but for simplicity you can send all requests to the same one (say, 8080). If the inspector you send the request to can't inspect the target image (because it doesn't have the right package manager), it redirects you to the one that can.

Supported package manager database formats: apk, dpkg (which apt also uses), and rpm (which yum also uses). 


### Trying hub-imageinspector-ws in a Docker environment ###

src/main/resources/demo-docker.sh is a shell script that uses docker to get 3 containers running, and then suggests (echo's) some curl commands to test the service.

Requirements: bash, docker, java 8, curl, port 8080, 8081, 8082, and a /tmp dir.

The script will start 3 imageinspectors. Each is a containerized web service. They'll run on ports 8080, 8081, and 8082. Each exposes a "getBdio" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns the list of components found in BDIO format (which can be uploaded to the Hub). 

You could send requests to any one of ports/containers and get the same result (assuming you follow redirects), but for simplicity you can send all requests to the same one (say, 8080). If the inspector you send the request to can't inspect the target image (because it doesn't have the right package manager), it redirects you to the one that can.

Supported package manager database formats: apk, dpkg (which apt also uses), and rpm (which yum also uses). 

### Endpoints ###

```
GET /getbdio?tarfile=<path to Docker image tarfile>
GET /trace # get history of http requests
GET /health # check health
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.blackducksoftware
```

