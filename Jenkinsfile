pipeline {
    agent any

    stages {
        stage('📋 Info') {
            steps {
                echo "════════════════════════════════════════"
                echo "Starting build #${env.BUILD_NUMBER}"
                echo "Branch: ${env.GIT_BRANCH}"
                echo "════════════════════════════════════════"

                sh 'java -version'
                sh './backend/mvnw -version'
            }
        }

        stage('Checkout & Setup') {
            steps {
                checkout scm
                echo "Building branch: ${env.GIT_BRANCH}"
            }
        }

        stage('Backend build & test') {
            steps {
                dir('backend') {
                    sh './mvnw -B -q clean install -T 2C'
                    echo "Backend build and tests completed successfully"
                }
            }
        }

        stage('Frontend build & test') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm test'
                    sh 'npm run build -- --configuration production'
                    echo "Frontend build and tests completed successfully"
                }
            }
        }

        stage('Docker Build') {
            steps {
            script {
                // Build all services
                sh 'docker compose build --parallel --no-cache'
                
                echo "Docker build completed successfully"
            }
            }
        }
        
        stage('Docker Deploy') {
            steps {
                script {
                    sh 'docker compose down'

                    // Deploy all services
                    sh 'docker compose up -d --remove-orphans'
                    
                    echo "Docker deployment completed successfully"
                }
            }
        }
    }
}