pipeline {

 agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  name: jenkins-slave
  namespace: jenkins
  labels:
    component: ci
    jenkins: jenkins-agent
spec:
  # Use service account that can deploy to all namespaces
  serviceAccountName: jenkins
  volumes:
  - name: dind-storage
    emptyDir: {}
  containers:
  - name: git
    image: alpine/git
    command:
    - cat
    tty: true
  - name: maven
    image: maven:latest
    command:
    - cat
    tty: true
  - name: kubectl
    image: lachlanevenson/k8s-kubectl:v1.8.8
    command:
    - cat
    tty: true
  - name: docker
    image: docker:19-git
    command:
    - cat
    tty: true
    env:
    - name: DOCKER_HOST
      value: tcp://docker-dind:2375
    volumeMounts:
      - name: dind-storage
        mountPath: /var/lib/docker
  - name: helm
    image: lachlanevenson/k8s-helm:latest
    command:
    - cat
    tty: true
"""
}
}


 stages {
        stage('RUN Unit Tests') {
        steps {
        container('maven') {
          sh "mvn install" ;
          sh "mvn test" ;
          }
        }
    }
  stage ('Helm create') {
   steps {
    container ('helm') {
        sh "helm version"
        sh "helm create java-web-app-chart" ;
    }
   }
  }


stage('Create Docker images') {
       steps{
        container('docker') {
         withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
            sh """
              docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}
              docker build -t kongurua/java-web-app:1 .
              docker push kongurua/java-web-app:1
               """
          }
        }
      }
    }
  }
}
