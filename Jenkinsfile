#!/usr/bin/env groovy

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

      stage('Checkout SCM') {
        checkout scm
        sh 'git rev-parse HEAD > GIT_COMMIT'
        shortCommit = readFile('GIT_COMMIT').take(7)
        echo "short tag: ${shortCommit}"
      }



//        sh 'echo GIT_SHA_SHORT=`git rev-parse --short=8 ${GIT_COMMIT}`'

/* uncomment if you need separate Tests
      stage('Unit Tests') {
        container('maven') {
          sh "mvn test" ;
          }
        }
*/
      stage('Building Application') {
        container('maven') {
          sh "mvn install"
          }
        }


// Docker Image Building
        // Environment variables DOCKERHUB_USER, DOCKERHUB_IMAGE
        // var info from Jenkins plugins:
        // BRANCH_NAME = PR-1    - pull request
        // BRANCH_NAME = develop - other branch
        // BRANCH_NAME = v0.0.1  - git tag
        // shortCommit for master branch (DEV release)
    stage('Docker build') {
      if  ( !isChangeSet() ) {
        container('docker') {
        if ( isMaster() ) {
               echo "Build docker image with tag ${shortCommit}"
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${shortCommit}  .'        
          }
        else
           withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
             sh  'echo "Create Docker image: ${DOCKERHUB_IMAGE}:${BRANCH_NAME}"'
             sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
            }
        }
      }
    }

// do not push docker image for PR
        if ( isPullRequest() ) {
            // exitAsSuccess()
            return 0
        }

// push docker image for all other cases (except PR)
    if  ( !isChangeSet() ) {
        stage ('Docker push') {
            container('docker') {
              withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'docker image ls'
                    sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
        }
    }

// do not deploy when 'push to branch' (and PR)
        if ( isPushtoFeatureBranch() ) {
                // exitAsSuccess()
                return 0
        }

// deploy

        if ( isChangeSet()  ) {
            stage('Deploy PROD release') {
                echo "Production release controlled by a change to production-release.txt file in application repository root,"
                echo "containing a git tag that should be released to production environment"
                tagDockerImage = "${sh(script:'cat production-release.txt',returnStdout: true)}"
                deployHelm("javawebapp-prod","prod",tagDockerImage)
                                    // image tag from file production-release.txt , namespace , name chart release
            }
        }
        else if ( isMaster() ) {
            stage('Deploy DEV release') {
                echo "Every commit to master branch is a dev release"
                echo "Deploy Dev release after commit to master"
                deployHelm("javawebapp-dev","dev",env.BRANCH_NAME)
            }
        }
        else if ( isBuildingTag() ){
// add check if it master
            stage('Deploy QA release') {
                echo "Every git tag on a master branch is a QA release"
                deployHelm( "javawebapp-qa","qa",env.BRANCH_NAME )
            }
        }
    } // node
  } //podTemplate


  // is it push to Master branch?
  def isMaster() {
      return (env.BRANCH_NAME == "master" )
  }

  def isPullRequest() {
      return (env.BRANCH_NAME ==~  /^PR-\d+$/)
  }

  def isBuildingTag() {
      return ( env.BRANCH_NAME ==~ /^\d.\d.\d$/ )
  }

  def isPushtoFeatureBranch() {
      return ( ! isMaster() && ! isBuildingTag() && ! isPullRequest() )
  }

  def isChangeSet() {

      def changeLogSets = currentBuild.changeSets
             for (int i = 0; i < changeLogSets.size(); i++) {
             def entries = changeLogSets[i].items
             for (int j = 0; j < entries.length; j++) {
                 def files = new ArrayList(entries[j].affectedFiles)
                 for (int k = 0; k < files.size(); k++) {
                     def file = files[k]
                     if (file.path.equals("production-release.txt")) {
                         return true
                     }
                 }
              }
      }

      return false
  }


// name = javawebapp
// ns = dev/qa/prod
// tag = image's tag

// !! need to change deployment version or label in order to re-deploy pod
  def deployHelm(name, ns, tag) {
     container('helm') {
        withKubeConfig([credentialsId: 'kubeconfig']) {
        sh """
            echo "Deployments is starting..."

            helm upgrade --install $name --debug ./javawebapp-chart \
            --force \
            --wait \
            --namespace $ns \
            --set image.repository=$DOCKERHUB_USER/$DOCKERHUB_IMAGE \
            --set-string ingress.hosts[0].host=${name}.ddns.net \
            --set-string ingress.tls[0].hosts[0]=${name}.ddns.net \
            --set-string ingress.tls[0].secretName=acme-$name-tls \
            --set image.tag=$tag

            helm ls
        """

        }
    }

}
