pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh "mvn clean compile"
            }
        }

        stage('Test') {
            steps {
               echo " Test stages..."
            }
        }
    }
}
