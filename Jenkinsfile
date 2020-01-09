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
      def nameStage

      stage('Checkout SCM') {
        checkout scm
        sh 'git rev-parse HEAD | cut -b 1-7 > GIT_COMMIT_SHORT'
        SHORT_COMMIT = readFile('GIT_COMMIT_SHORT')
        echo "Short Commit: ${SHORT_COMMIT}"
      }


//
// *** Test and build Java Web App
//
/*
      stage('Unit Tests') {
        container('maven') {
          sh "mvn test"
          }
        }
*/
      stage('Building Application') {
        container('maven') {
          sh "mvn install"
          }
        }


/* working / tested
//// TMP triggering a remote Job
stage('Triggering a remote Job') {
  def job
    echo "Triggering a remote Job"
    echo "From Folder"
    build job:'IBM_Project/DeployJavaWebApp/master'
  }
*/



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
    stage('Docker build') {
    container('docker') {
      if  ( !isChangeSet() ) {
        if ( isMaster() ) {
          tagDockerImage = readFile('GIT_COMMIT_SHORT')
          echo  "From Short ${tagDockerImage}" //use short commit for master
        }
        else {
          tagDockerImage = "${BRANCH_NAME}"
          echo  "From Branch ${tagDockerImage}" //testing
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
    }

//
// *** Docker Image Push
//
// push docker image for all other cases (except PR & Prod)
    stage ('Docker push') {
      container('docker') {
        if  ( !isChangeSet() ) {
          if ( isPullRequest() ) {
            // exitAsSuccess()
            return 0
          }
          else {
            withCredentials([usernamePassword(credentialsId: 'docker_hub_login', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
              sh 'docker image ls'
              sh "docker push ${DOCKERHUB_USER}/${DOCKERHUB_IMAGE}:${tagDockerImage}" // ${tagDockerImage}<==>${BRANCH_NAME}
            }
          }
        }
      }
    }


/*  Put Deploy to different repo / project
//
// *** Helm Deploy
//
// do not deploy when 'push to branch' (and PR)
        if ( isPushtoFeatureBranch() ) {
                // exitAsSuccess()
                return 0
        }
        if ( isChangeSet()  ) {
            stage('Deploy PROD release') {
                echo "Production release controlled by a change to production-release.txt file in application repository root,"
                echo "containing a git tag that should be released to production environment"
                tagDockerImage = "${sh(script:'cat production-release.txt',returnStdout: true)}"
                deployHelm("javawebapp-prod2","prod",tagDockerImage)
            }
        }
        else if ( isMaster() ) {
            stage('Deploy DEV release') {
                echo "Every commit to master branch is a dev release"
                echo "Deploy Dev release after commit to master"
                deployHelm("javawebapp-dev2","dev",tagDockerImage)
            }
        }
        else if ( isBuildingTag() ){
// add check if it master
            stage('Deploy QA release') {
                echo "Every git tag on a master branch is a QA release"
                deployHelm( "javawebapp-qa2","qa",env.BRANCH_NAME )
            }
        }
*/
    def job // check if it is needed
    stage('Triggering Deploymant Job') {
  // do not deploy when 'push to branch' (and PR)
          if ( isPushtoFeatureBranch() ) {
                  // exitAsSuccess()
                  return 0
          }
          // no PROD release here

          // Dev - trigger Deploy repo with Parameters: master
          // QA - trigger Deploy repo with Parameters: tag

          if ( isMaster() )  {
                  echo "Triggering DEPLOY repo for DEV release with Parameters: master "
                  echo "SHOW ${tagDockerImage}"
                  build job:'IBM_Project/DeployJavaWebApp/master',
                  parameters: [string(name: 'DEPLOY_TAG', value: SHORT_COMMIT),string(name:'BRANCHNAME',value:env.BRANCH_NAME)]

          }
          else if ( isBuildingTag() ){
                  echo "Triggering DEPLOY repo for QA release with Parameters: tag "
                  build job:'IBM_Project/DeployJavaWebApp/master',
                  parameters: [string(name: 'DEPLOY_TAG', value: SHORT_COMMIT),string(name:'BRANCHNAME',value:env.BRANCH_NAME)] // passed "tag" value
          }

    }

    } // node
  } //podTemplate


//
// *** Functions
//

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

/* new version - need testing
    currentBuild.changeSets.any { changeSet ->
          changeSet.items.any { entry ->
            entry.affectedFiles.any { file ->
              if (file.path.equals("production-release.txt")) {
                return true
              }
            }
          }
        }

*/
// old version
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
//
      return false
  }


//  Put Deploy to different repo / project
//
// Deployment function
/*
// name = javawebapp
// ns = dev/qa/prod
// tag = image's tag
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
            --set-string ingress.tls[0].secretName=acme-${name}-tls \
            --set image.tag=$tag
            helm ls
        """
        }
    }
  }

*/
