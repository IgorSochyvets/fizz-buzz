# java-web-app

# Description
This is CI/CD project realized using Jenkins
## Requirements
* K8s cluster
* Helm/tiller
* Kubectl
* Pipeline configuration:
  ```
  Jenkinsfile
  ```
## Architecture
* Create test Java web conteiner (Spring Boot) application "Hello World" (using video guide: https://www.youtube.com/watch?v=FlSup_eelYE):

  ```
  *.jar
  ```
* Create docker container with test app
  ```
  docker build -f Dockerfile -t kongurua/java-web-app:latest .
  docker run -p 80:80 kongurua/java-web-app
  docker push kongurua/java-web-app:latest
  ```
* Create helm chart with with test app
* Create Jenkins pipepine for CI/CD process using with test app


* test line 5
