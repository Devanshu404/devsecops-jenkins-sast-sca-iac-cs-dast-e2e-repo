@Library('your-shared-library') _

pipeline {
    agent any

    stages {
        stage('Run Gitleaks Scan') {
            steps {
                script {
                    gitleaksScan('.', '', true)
                }
            }
        }
    }
}
