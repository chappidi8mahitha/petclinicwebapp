pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/chappidi8mahitha/petclinicwebapp.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Deploy') {
            steps {
                sh 'mvn deploy'
            }
        }
    }
}
