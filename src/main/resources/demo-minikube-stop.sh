#!/bin/bash

deploymentName=hub-imageinspector-ws
serviceName=hub-imageinspector-ws

echo "--------------------------------------------------------------"
echo "Deleting deployment, service"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}"
kubectl delete deployment "${deploymentName}"
