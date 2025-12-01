pipeline {
    agent any

    stages {
        stage('Checkout & Setup') {
            steps {
                checkout scm
                echo "Building branch: ${env.BRANCH_NAME}"
            }
        }
    }
}