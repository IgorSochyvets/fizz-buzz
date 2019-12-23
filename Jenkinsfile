#!/usr/bin/env groovy


env.DOCKERHUB_IMAGE = 'nginx-test'
env.DOCKERHUB_USER = 'kongurua'
env.DEV_RELEASE_TAG = 'dev'
env.QA_RELEASE_TAG = 'qa'
env.PROD_RELEASE_TAG = 'prod'


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




// Docker Image Building
        // Environment variables DOCKERHUB_USER, DOCKERHUB_IMAGE
        // var info from Jenkins plugins:
        // BRANCH_NAME = master  - master branch
        // BRANCH_NAME = PR-1    - pull request
        // BRANCH_NAME = develop - other branch
        // BRANCH_NAME = v0.0.1  - git tag
        //
    stage('Creating Docker Image') {
            container('docker') {
             withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
               sh  'echo "Create Docker image: ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}"'
               sh  'docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}'
               sh  'docker build -t ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME} .'
//               sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
        }

        if ( isPullRequest() ) {
            // exitAsSuccess()
            return 0
        }

        stage ('Docker push') {
            container('docker') {
              withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'docker image ls'
                    sh  'docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${BRANCH_NAME}'
              }
            }
        }


        if ( isPushtoFeatureBranch() ) {
                // exitAsSuccess()
                return 0
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
      // add check that  is branch master?
      return ( env.BRANCH_NAME ==~ /^v\d.\d.\d$/ )
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

/*
  def deploy( tagName, appName ) {

          echo "Release image: ${DOCKER_IMAGE_NAME}:$tagName"
          echo "Deploy app name: $appName"

          withKubeConfig([credentialsId: 'kubeconfig']) {
          sh"""
              kubectl delete deploy ${appName} --wait -n jenkins
              kubectl delete svc ${appName} --wait -n jenkins
              kubectl run ${appName} -n jenkins --image=${DOCKER_IMAGE_NAME}:${tagName} \
                  --port=3000 --labels="app=$appName" --image-pull-policy=Always \
                  --env="INPUT_VERSION=$appName"
              kubectl expose -n jenkins deploy/${appName} --port=3000 --target-port=3000 --type=NodePort
              kubectl get svc -n jenkins
          """
          }

  }

*/


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
