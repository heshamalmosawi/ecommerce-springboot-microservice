# Sonar Qube Setup & Notes

1. Pull the latest image
```sh
docker pull sonarqube:latest
```

2. Run a container
- If new:
```sh
docker run --name sonarqube -p 9000:9000 sonarqube:latest
```
- If not new
```sh
docker run -p 9000:9000 sonarqube
```
