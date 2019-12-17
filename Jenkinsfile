pipeline {

  environment {
      DOCKERHUB_IMAGE = 'nginx-test'
      DOCKERHUB_USER = 'kongurua'
      DEV_RELEASE_TAG = 'dev'
      QA_RELEASE_TAG = 'qa'
  }

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


// do next stage after every commit to every branch;
/*
        stage('RUN Unit Tests') {
        steps {
        container('maven') {
          sh "mvn install"
          sh "mvn test" ;
          }
        }
    }
*/


// dev
// QA
// prod
// PR
// branch



// Every commit to master branch is a dev release
    stage('Create Docker images for DEV release') {
           when {
                branch 'master'
            }
           steps{
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
               sh  'echo ${TAG_NAME}'
               sh 'echo ${BRANCH_NAME}'
               sh 'echo ${CHANGE_ID}'
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${DEV_RELEASE_TAG} .'
               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${DEV_RELEASE_TAG}'
              }
            }
          }
        }



// Every git tag on a master branch is a QA release
        stage('Create Docker images for QA release') {
          when { not
           {
               anyOf {
                   branch 'development'
                   branch 'feature-1'
                   branch 'feature-2'
                   branch 'master'
               }
           }
         }
               steps{
                container('docker') {
                 withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                   sh  'echo ${TAG_NAME}'
                   sh 'echo ${BRANCH_NAME}'
                   sh 'echo ${CHANGE_ID}'
                   sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
                   sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
                   sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
                  }
                }
              }
            }




// Every git tag on a master branch is a QA release
//
//
/*
stage('Create Docker images for QA release') {
     when {
         tag "release-*"
      }
       steps{
        container('docker') {
         withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
           sh  'echo ${TAG_NAME}'
           sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
           sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${QA_RELEASE_TAG} .'
           sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${QA_RELEASE_TAG}'
          }
        }
      }
    }
*/



// Production release controlled by a change to production-release.txt file in application repository root, containing a git tag that should be released to production environment


// next stage works after commit to every branch
    stage('Create Docker images for Branches') {
           when {
                anyOf {
                    branch 'development'
                    branch 'feature-1'
                    branch 'feature-2'
                    environment name: 'DEPLOY_TO', value: 'production'
                }
            }
           steps{
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
          }
        }


// next stage works after PR
        stage('Create Docker images for PR') {
              when {
                              expression { BRANCH_NAME =~ 'PR-*' }
              }
               steps{
                container('docker') {
                 withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                   sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
                   sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
                   sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
                  }
                }
              }
            }



// do next stage after pushin docker image and before deployment
    /*
      stage ('Helm create') {
       steps {
        container ('helm') {
            sh "helm version"
            sh "helm create java-web-app-chart" ;
        }
       }
      }
    */


// Deployment stage


  }
  post {
      always {
          echo 'This is a post message!!'
      }
  }
}
