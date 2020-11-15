#!/bin/bash
mvn clean package -Dmaven.javadoc.skip=true
docker build -t mobilecs-backend ./backend
docker build -t mobilecs-flink ./flink
