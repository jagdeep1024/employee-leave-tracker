#!/usr/bin/env sh
set -eu

TAG="${1:-latest}"

docker login

docker build -f discovery-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-discovery-service:latest .
docker build -f auth-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-auth-service:latest .
docker build -f employee-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-employee-service:latest .
docker build -f leave-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-leave-service:latest .
docker build -f notification-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-notification-service:latest .
docker build -f api-gateway/Dockerfile -t jagcoool/nagp-employee-leave-tracker-api-gateway:latest .
docker build -f ui/Dockerfile -t jagcoool/nagp-employee-leave-tracker-ui:latest ui

for service in discovery-service auth-service employee-service leave-service notification-service api-gateway ui; do
  repo="jagcoool/nagp-employee-leave-tracker-$service"
  if [ "$TAG" != "latest" ]; then
    docker tag "${repo}:latest" "${repo}:${TAG}"
    docker push "${repo}:${TAG}"
  else
    docker push "${repo}:latest"
  fi
done
