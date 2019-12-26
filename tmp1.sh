#!/bin/bash
# this script checks if exist deployment
# it is tgemp script

helm list | grep javawebapp | awk '{print $1}'" == "javawebapp"

if "helm list | grep javawebapp | awk '{print $1}'" == "javawebapp"; then
echo "chart exists"
elif [['helm list | grep javawebapp | awk '{print $1}'' == ""]]; then  
echo "no chart installed"
fi
