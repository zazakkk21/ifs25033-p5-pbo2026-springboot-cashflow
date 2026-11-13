pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B test'
                    } else {
                        bat 'mvnw.cmd -B test'
                    }
                }
            }
        }
    }
}
