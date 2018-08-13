#!/bin/bash

deploymentName=blackduck-imageinspector
serviceName=blackduck-imageinspector

echo "--------------------------------------------------------------"
echo "Deleting deployment, service"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}"
kubectl delete deployment "${deploymentName}"
