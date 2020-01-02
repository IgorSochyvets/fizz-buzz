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
  docker build -f Dockerfile -t kongurua/fizz-buzz:latest .
  docker run -p 80:80 kongurua/fizz-buzz:latest
  docker push kongurua/fizz-buzz:latest
  ```
* Create helm chart with with test app
* Create Jenkins pipepine for CI/CD process using with test app


* test line 1
* test line 2
* this is from branch feature-8


# Demo

1. git checkout -b feature-9
2. Do some changes (v+1 for app)
3. git add .
4. git push origin feature-9
5. Check if new docker image been created (fizz-buzz:feature-9)
6. Go to GitHub and create PR
7. Check Jenkins logs logs ( docker build fizz-buzz:PR-<NN> / no push)
8. Git checkout master
9. Do some changes (Hello DevOps v +1)
10. Git commit -a -m “Dev v +1” && git push origin master
11. Check Dev web app
12. Git tag 2.0.<n+1> && git push origin 2.0.<n+1>
13. Check QA web app
14. “echo 2.0.<n+1>” > production-release.txt
15. Git commit -a -m “Prod  2.0.<n+1>” && git push origin master
16. Show Unit test (commit to master > DEV release)
