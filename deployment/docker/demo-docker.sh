#!/bin/bash

docker pull blackducksoftware/blackduck-imageinspector-alpine:6.2.1
docker pull blackducksoftware/blackduck-imageinspector-ubuntu:6.2.1

mkdir /tmp/imageinspector
docker pull alpine:latest
docker save -o /tmp/imageinspector/alpine.tar alpine:latest

docker pull fedora:latest # rpm images are unsupported and return empty BDIO
docker save -o /tmp/imageinspector/fedora.tar fedora:latest

docker pull debian:latest # will run on ubuntu
docker save -o /tmp/imageinspector/debian.tar debian:latest

echo Starting 2 imageinspector containers...
docker run -v /tmp/imageinspector:/opt/blackduck/hub-imageinspector-ws/target -d --name hub-imageinspector-ws-alpine -p 8080:8080 blackducksoftware/blackduck-imageinspector-alpine:6.2.1
docker run -v /tmp/imageinspector:/opt/blackduck/hub-imageinspector-ws/target -d --name hub-imageinspector-ws-ubuntu -p 8082:8082 blackducksoftware/blackduck-imageinspector-ubuntu:6.2.1

echo "Suggested tests:"
echo curl -i http://localhost:8080/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/alpine.tar
echo curl -i http://localhost:8080/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/fedora.tar
echo curl -i http://localhost:8080/getbdio?tarfile=/opt/blackduck/hub-imageinspector-ws/target/debian.tar
echo ""
echo "The first request will get the result (BDIO) directly (because the package managers line up)."
echo "The fedora request returns HTTP 200 with empty BDIO because rpm is unsupported."
echo "For the debian request: following the redirect will get you the result"
