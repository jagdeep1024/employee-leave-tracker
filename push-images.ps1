param(
  [string]$Tag = "latest"
)

$images = @(
  "discovery-service",
  "auth-service",
  "employee-service",
  "leave-service",
  "notification-service",
  "api-gateway",
  "ui"
)

docker login

docker build -f discovery-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-discovery-service:latest .
docker build -f auth-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-auth-service:latest .
docker build -f employee-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-employee-service:latest .
docker build -f leave-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-leave-service:latest .
docker build -f notification-service/Dockerfile -t jagcoool/nagp-employee-leave-tracker-notification-service:latest .
docker build -f api-gateway/Dockerfile -t jagcoool/nagp-employee-leave-tracker-api-gateway:latest .
docker build -f ui/Dockerfile -t jagcoool/nagp-employee-leave-tracker-ui:latest ui

foreach ($service in $images) {
  $repo = "jagcoool/nagp-employee-leave-tracker-$service"
  if ($Tag -ne "latest") {
    docker tag "$repo`:latest" "$repo`:$Tag"
    docker push "$repo`:$Tag"
  } else {
    docker push "$repo`:latest"
  }
}
