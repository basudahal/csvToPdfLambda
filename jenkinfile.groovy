import jenkins.model.*
jenkins = Jenkins.instance
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
        script {
          env.AWS_ACCESS_KEY_ID = ''
          env.AWS_SECRET_ACCESS_KEY = ''
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "${deploymentCredentialId}", accessKeyVariable:'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
            def assume_result = sh returnStdout:true, script: "aws sts assume-role --role-arn arn:aws:iam::${deployAccount}:role/${deploymentRole} --role-session-name ${projectName}-deploy"
            def assume_object = Json.parse(assume_result)
            env.AWS_ACCESS_KEY_ID = assume_object.Credentials.AccessKeyId
            env.AWS_SECRET_ACCESS_KEY = assume_object.Credentials.SecretAccessKey
          }
        }
        sh deployCommand
      }
    }
  }
}