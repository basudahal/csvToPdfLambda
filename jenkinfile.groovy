import groovy.transform.Field

pipeline {
  agent any

  environment {
    subject = ''
    projectName = 'lambda-csvToPdf'
    deploymentRole = 'deployment-role'
    deploymentCredentialId = 'jenkin_svc'
    deploymentAccount = '878955458484'
  }
  parameters {
    string(description: 'dev tag', name: 'dev_tag')
    choice(name: 'region', choices: ['us-east-1', 'us-east-2'], description: 'Region to deploy')
    choice(name: 'environment', choices: [dev], description: 'Environment to deploy')
  }

  stages{
    stage('Prepare'){
      steps {
        buildName "${params.dev_tag}-${params.environment}"
        sh "git checkout ${params.dev_tag}"
        script {
          deployCommand = "serverless deploy"
          deployAccount = deploymentAccount
        }
      }
    }
    stage('npm install') {
      steps {
        sh "npm install"
      }
    }
    stage('Deploy') {
      steps {
        sh deployCommand
      }
    }
  }
}