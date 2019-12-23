#!/usr/bin/env groovy


// not working / for Environment variables
//import hudson.model.*
//def hardcoded_param = "FOOBAR"
//def resolver = build.buildVariableResolver
//def hardcoded_param_value = resolver.resolve(hardcoded_param)
env.DB_URL="hello"

/*
pipeline {

  environment {
      DOCKERHUB_IMAGE = 'nginx-test'
      DOCKERHUB_USER = 'kongurua'
      DEV_RELEASE_TAG = 'dev'
      QA_RELEASE_TAG = 'qa'
      PROD_RELEASE_TAG = 'prod'
  }
  */

def label = "jenkins-agent"

podTemplate(label: label, yaml: """
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
  ){

    node(label) {


      stage('Checkout SCM') {
        checkout scm
      }


/* working / tested
      stage('Unit Tests') {
        container('maven') {
          sh "mvn test" ;
          }
        }
*/

/* working / tested
        stage('Building Application') {
          container('maven') {
            sh "mvn install"
            }
          }
*/

stage('Building Application') {
  container('maven') {
    sh 'echo "Test ENV VAR !!!"'
    sh 'echo "${env.DB_URL}"'
    }
  }

// dev
// Every commit to master branch is a dev release
    stage('Create Docker images for DEV release') {
//         when {
//                branch 'master'
//            }
//           steps{
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
//               sh  'println "param ${hardcoded_param} value : ${hardcoded_param_value}"'
               sh  'echo "Create Docker images for DEV release"'
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
//          }
        }




    } // node
  } //podTemplate



// stages {


/*
// dev
// Every commit to master branch is a dev release
    stage('Create Docker images for DEV release') {
           when {
                branch 'master'
            }
           steps{
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
               sh  'echo "Create Docker images for DEV release"'
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
          }
        }


// QA
// Every git tag on a master branch is a QA release
        stage('Create Docker images for QA release') {
          when { not
           {
               anyOf {
                    // Put here ALL branches!!!
                   branch 'development'
                   branch 'feature-*'
                   branch 'master'
                   branch 'PR-*'
               }
           }
         }
               steps{
                container('docker') {
                 withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                   sh  'echo "Create Docker images for QA release"'
                   sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
                   sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
                   sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
                  }
                }
              }
            }


// prod
// Production release controlled by a change to production-release.txt file in application repository root, containing a git tag that should be released to production environment

stage('Create Docker images for PROD release') {
  when {
       allOf {
           changeset "production-release.txt"
           branch 'master'
       }
 }
       steps{
        container('docker') {
         withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
//           sh  'PROD_RELEASE_TAG1=`cat production-release.txt`'
//           sh  'PROD_RELEASE_TAG="${sh(script:'cat production-release.txt',returnStdout: true)}"'
           sh  'echo "Create Docker images for PROD release"'
           sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
           sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:`cat production-release.txt` .'
           sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:`cat production-release.txt`'
          }
        }
      }
    }



// branch
// Every branch that is not also a PR should have build, test, docker image build, docker image push steps with docker image tag = branch name
// next stage works after commit to every branch
    stage('Create Docker images for Branches') {
           when {
                anyOf {
                    // Put here ALL branches!!! without "master"
                    branch 'development'
                    branch 'feature-*'
                }
            }
           steps{
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
               sh  'echo "Create Docker images for Branch release"'
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
          }
        }


// PR
// Every PR should have build, test, docker image build, docker image push steps with docker tag = pr-number
// next stage works after PR
        stage('Create Docker images for PR') {
              when {
                              expression { BRANCH_NAME =~ 'PR-*' }
              }
               steps{
                container('docker') {
                 withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                   sh  'echo "Create Docker images for PR release"'
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
            sh "helm create java-web-app-chart"
        }
       }
      }



    // Test Kubeconfig

          stage ('Test Kubeconfig') {
           steps {
            container ('kubectl') {
                withKubeConfig([credentialsId: 'kubeconfig']) {
                  sh 'echo "Test Kubeconfig"'
                  sh 'kubectl get ns'
                }
            }
           }
          }

// Deployment stage

  }
  post {
      always {
          echo 'This is a post message!!'
      }
  }
}

*/
