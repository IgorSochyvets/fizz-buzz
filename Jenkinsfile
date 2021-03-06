#!/usr/bin/env groovy

import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

env.DOCKERHUB_IMAGE = 'fizz-buzz'
env.DOCKERHUB_USER = 'kongurua'

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
    image: lachlanevenson/k8s-helm:v2.16.1
    command:
    - cat
    tty: true
"""
  ){

node(label) {

  def tagDockerImage

  stage('CheckoutSCM') {
    checkout scm
    tagDockerImage = sh(returnStdout: true, script: "git rev-parse HEAD").trim().take(7)
    echo "Short Commit: ${tagDockerImage}"
  }

//
// *** Test and build Java Web App
//
  stage('UnitTests') {
    container('maven') {
    sh 'ls target'
    sh 'mvn test'
    }
  }

  stage('BuildingApp') {
    container('maven') {
    sh 'ls target'
    sh 'mvn install'
    }
  }

//
// *** Docker Image Building
//
        // Description of app's logic
        // BRANCH_NAME = master  - master branch / It is DEV release / tag=short_commit
        // BRANCH_NAME = PR-1    - pull request  / It is PR request  / tag=PR-1 /
        // BRANCH_NAME = develop - other branch  / feature develop   / tag=<branch_name>
        // BRANCH_NAME = v0.0.1  - git tag       / It is QA release  / tag=short_commit (the same image from dev release)
        // change file to mark prod release      /It is PROD release / tag=short_commit
        // do docker buils for all cases except "prod" release = !isChangeset
  stage('DockerBuild') {
    container('docker') {
        if ( isMaster() ) {
          echo  "From Short ${tagDockerImage}" //use short commit for master
        }
        else {
          tagDockerImage = "${BRANCH_NAME}"
          echo  "From Branch ${tagDockerImage}"
        }
// if master then tagDockerImage = short_commit
//else   tagDockerImage = branch_name
        withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
          echo "Create Docker image: ${DOCKERHUB_IMAGE}:${tagDockerImage}"
          sh "docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}"
          sh "docker build . -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${tagDockerImage}"
        }
    }
  }

//
// *** Docker Image Push
//
// push docker image for all other cases (except PR & Prod)
  stage ('DockerPush') {
    container('docker') {
        if ( !isPullRequest() ) {
          withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
            sh 'docker image ls'
            sh "docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${tagDockerImage}"
          }
        }
        else Utils.markStageSkippedForConditional('DockerPush')
    }
  }

/// ***  use one 'build job' instead of two
//    def job
  stage('TriggerDeployJob') {
  // do not deploy when 'push to branch' (and PR)
    if ( !isPushtoFeatureBranch() ) {
          if ( isMaster() || isBuildingTag() )  {
                  build job:'IBM_Project/DeployJavaWebApp/master',
                  parameters: [string(name: 'deployTag', value: tagDockerImage)], wait: false // wait: false (default value - true)
    }
    else Utils.markStageSkippedForConditional('TriggerDeployJob')
  }
}

} // node
} //podTemplate


// *** Functions
def isMaster() {
  return (env.BRANCH_NAME == "master" )
}

def isPullRequest() {
  return (env.BRANCH_NAME ==~  /^PR-\d+$/)
}

def isBuildingTag() {
  return ( env.BRANCH_NAME ==~ /^\d+.\d+.\d+$/ )   //// add \.
}

def isPushtoFeatureBranch() {
  return ( ! isMaster() && ! isBuildingTag() && ! isPullRequest() )
}
