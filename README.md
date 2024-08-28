# Overview #
A container-based Web Service for analyzing Docker images.

## Build ##
[![Build Status](https://travis-ci.org/blackducksoftware/hub-imageinspector-ws.svg?branch=master)](https://travis-ci.org/blackducksoftware/hub-imageinspector-ws)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.blackduck.integration%3Ahub-imageinspector-ws&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.blackduck.integration%3Ahub-imageinspector-ws)
[![Coverage Status](https://coveralls.io/repos/github/blackducksoftware/hub-imageinspector-ws/badge.svg?branch=master)](https://coveralls.io/github/blackducksoftware/hub-imageinspector-ws?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Where can I get the latest release? #
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-imageinspector-ws. 

To try it in a Docker environment, you can use this bash script as a starting point: https://github.com/blackducksoftware/hub-imageinspector-ws/blob/master/src/main/resources/demo-docker.sh.

Ty try it in a Kubernetes environment, you use this bash script as a starting point: https://github.com/blackducksoftware/hub-imageinspector-ws/blob/master/src/main/resources/demo-minikube.sh. It depends on: https://github.com/blackducksoftware/hub-imageinspector-ws/blob/master/src/main/resources/kube-deployment.yml, https://github.com/blackducksoftware/hub-imageinspector-ws/blob/master/src/main/resources/kube-service.yml.

# Documentation #
hub-imageinspector-ws is a simple container-based web service that, given a path to a docker image tarfile, returns a Black Duck Hub BDIO (Bill Of Materials) file.

You can use provided bash scripts as sample code that shows how to deploy and use hub-imageinspector-ws in either a Kubernetes or a Docker environment. You only need files in the src/main/resources directory. Whichever script you use, you'll want to read the script to understand what it's doing. 

The Docker images (blackducksoftware/hub-imageinspector-ws-alpine, blackducksoftware/hub-imageinspector-ws-centos, and blackducksoftware/hub-imageinspector-ws-ubuntu) are available on Docker Hub. 

### ImageInspector Service Endpoint ###

GET /getbdio
* Mandatory query param: tarfile=`<path to Docker image tarfile>`
* Optional query params:
```
hubprojectname=<Hub project name>
hubprojectversion=<Hub project version>
codelocationprefix=<Hub CodeLocation name prefix>
cleanup=<cleanup working dirs when done: true or false; default: true>
```

### Trying hub-imageinspector-ws in a Kubernetes (minikube) environment ##

src/main/resources/demo-minikube.sh is a shell script that uses minikube to get a pod running, and then executes (and echo's) some curl commands to test the service.

Requirements: bash, minikube, java 8, curl, port 8080, 8081, 8082. It creates a ~/tmp/target dir.

The script will start a 3-container pod, and expose ports 8080, 8081, and 8082. Each port has a "getbdio" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns the list of components found in BDIO format (which can be uploaded to the Hub). 

You could send requests to any one of ports and get the same result (assuming you follow redirects), but for simplicity you can send all requests to the same one (say, 8080). If the inspector you send the request to can't inspect the target image (because it doesn't have the right package manager), it redirects you to the one that can.

Supported package manager database formats: apk, dpkg (which apt also uses), and rpm (which yum also uses). 

### Trying hub-imageinspector-ws in a Docker environment ###

src/main/resources/demo-docker.sh is a shell script that uses docker to get 3 containers running, and then suggests (echo's) some curl commands to test the service.

Requirements: bash, docker, java 8, curl, port 8080, 8081, 8082, and a /tmp dir.

The script will start 3 imageinspectors. Each is a containerized web service. They'll run on ports 8080, 8081, and 8082. Each exposes a "getbdio" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns the list of components found in BDIO format (which can be uploaded to the Hub). 

You could send requests to any one of ports/containers and get the same result (assuming you follow redirects), but for simplicity you can send all requests to the same one (say, 8080). If the inspector you send the request to can't inspect the target image (because it doesn't have the right package manager), it redirects you to the one that can.

Supported package manager database formats: apk, dpkg (which apt also uses), and rpm (which yum also uses). 

### Other ImageInspector Service Endpoints ###

```
GET /health # check the health of the service in JSON format.
GET /httptrace # a list of requests to the service in JSON format.
GET /metrics # get Spring Boot-generated metrics endpoints in JSON format. Request metric value with /metrics/<metric name>
GET /prometheus # get Prometheus-generated metrics in Prometheus format.
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.synopsys
```

### Optimizing Performance ###

Each service (port) handles a different family of linux distribution based on package manager database format:
* port 8080 inspects Linux images that use the apk package manager database format (used by apk).
* port 8081 inspects Linux images that use the rpm package manager database format (used by rpm and yum).
* port 8082 inspects Linux images that use the dpkg package manager database format (used by dpkg and apt).

Any mis-directed request (any request sent to one port that can/must be handled by a different port) will be redirected to the correct port. Correcly-directed requests (sent initially to the port that can handle the image) are faster and more efficient than mis-directed requests.

If you know (or learn) that most of your images are being handled by a port other than the one you are directing requests too, you can decrease response time and system load by directing requests to the port that is most often the correct port to handle your portfolio of images. You can determine this by examining the information returned by either of the two metrics endpoints: /metrics and /prometheus. 
* counter.status.200.getbdio tells you how many inspection requests this port handled. 
* counter.status.302.getbdio tells you how many inspection requests this port redirected to a different port.
(If you are using the /prometheus endpoint, your counter names will use '_' instead of '.'.)
