pipeline {
  agent any
  tools {
    maven 'Maven_9_9_9'
  }

  stages {
    stage('CompileandRunSonarAnalysis') {
      steps {
        withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
          bat("mvn -Dmaven.test.failure.ignore verify sonar:sonar -Dsonar.login=$SONAR_TOKEN -Dsonar.projectKey=easybuggy -Dsonar.host.url=http://localhost:9000/")
        }
      }
    }
    stage('Build') {
      steps {
        withDockerRegistry([credentialsId: "dockerlogin", url: ""]) {
          script {
            app = docker.build("Devanshu404/testeb")
          }
        }
      }
    }
    stage('RunContainerScan') {
      steps {
        withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN')]) {
          script {
            // try {
              bat("D://Newfolder//DevSecOps//SnykCLI//snyk-win.exe  container test Devanshu404/testeb")
            // } catch (err) {
            //   echo err.getMessage()
            // }
          }
        }
      }
    }
    stage('RunSnykSCA') {
      steps {
        withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN')]) {
          bat("mvn snyk:test -fn")
        }
      }
    }
    stage('RunDASTUsingZAP') {
      steps {
         bat("D://Newfolder//DevSecOps//EndToEndpipelines//ZAP_2.16.0_Crossplatform//ZAP_2.16.0//zap.sh -port 9393 -cmd -quickurl https://www.example.com -quickprogress -quickout D://Newfolder//DevSecOps//EndToEndpipelines//ZAP_2.16.0_Crossplatform//ZAP_2.16.0//output.html")              
      }
    }
    stage('Install Checkov') {
      steps {
          bat("pip install checkov")
        }
    }  
    stage('checkov') {
      steps {
        bat("checkov -s -f main.tf")
      }
    }

  }
}
