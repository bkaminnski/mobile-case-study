#!/bin/bash
mvn clean package -Dmaven.javadoc.skip=true
docker build -t bkaminnski/mobilecs .
