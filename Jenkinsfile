pipeline {
    agent any

    stages {

        stage('Checkout & Setup') {
            steps {
                checkout scm
                echo "Building branch: ${env.GIT_BRANCH}"
            }
        }

        stage('Backend build & test') {
            parallel {
                stage('Eureka SD') {
                    steps {
                        dir('backend/eureka-service-discovery') {
                            sh './mvnw clean test'
                            echo "Build and test completed for Eureka service discovery"
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('backend/apigateway') {
                            sh './mvnw clean test'
                            echo "Build and test completed for API Gateway"
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        dir('backend/user-service') {
                            sh './mvnw clean test'
                            echo "Build and test completed for User Service"
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        dir('backend/product-service') {
                            sh './mvnw clean test'
                            echo "Build and test completed for Product Service"
                        }
                    }
                }
                stage('Media Service') {
                    steps {
                        dir('backend/media-service') {
                            sh './mvnw clean test'
                            echo "Build and test completed for Media Service"
                        }
                    }
                }
            }
        }
    }
}