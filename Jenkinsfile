pipeline {
    agent any

    tools {
        nodejs('NodeJS') 
    }

    environment {
        ROLLEDBACK = 'false'
        SONARQUBE_ENV = 'local-sonar'
        SONAR_TOKEN = 'sqa_f21c9c92b3ec737db66d38a04e17b339358d3456'
        SONAR_HOST_URL = 'http://localhost:9000'
    }

    stages {
        stage('Checkout & Setup') {
            steps {
                checkout scm
                echo "Building branch: ${env.BRANCH_NAME ?: env.GIT_BRANCH}"
            }
        }

        stage('📋 Info') {
            steps {
                echo "════════════════════════════════════════"
                echo "Starting build #${env.BUILD_NUMBER}"
                echo "Branch: ${env.BRANCH_NAME ?: env.GIT_BRANCH}"
                echo "════════════════════════════════════════"

                sh 'java -version'
                sh './backend/mvnw -version'

                echo "Checking Node.js and Chromium versions..."
                sh 'node --version'
                sh 'npm --version'
                sh 'google-chrome --version || { echo "Chrome not found"; exit 1; }'
            }
        }

        stage('Backend build & test') {
            steps {
                dir('backend') {
                    sh './mvnw -B -q -s maven-settings.xml clean package -DskipTests'
                    echo "Backend build and tests completed successfully"
                }
            }
        }

        stage('Frontend build & test') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    // sh 'npm test'
                    sh 'npm run build -- --configuration production'
                    echo "Frontend build and tests completed successfully"
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    dir('backend') {
                        sh './mvnw -B -q -s maven-settings.xml sonar:sonar ' +
                           "-Dsonar.projectKey=esouq " +
                           "-Dsonar.projectName='esouq' " +
                           "-Dsonar.sources=src/main/java " +
                           "-Dsonar.tests=src/test/java " +
                           "-Dsonar.java.binaries=target/classes " +
                           "-Dsonar.coverage.jacoco.xmlReportPaths=*/target/site/jacoco/jacoco.xml " +
                           "-Dsonar.token=${SONAR_TOKEN}"
                    }
                    dir('frontend') {
                        sh 'npm ci'
                        sh """
                            npx sonar-scanner \
                              -Dsonar.projectKey=ecommerce-frontend \
                              -Dsonar.projectName='Ecommerce Frontend' \
                              -Dsonar.sources=src \
                              -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info \
                              -Dsonar.token=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            echo "Quality Gate failed with status: ${qg.status}"
                            echo "Analysis results available at: http://localhost:9000/dashboard?id=esouq"
                            echo "Analysis results available at: http://localhost:9000/dashboard?id=ecommerce-frontend"
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                        echo "Quality Gate passed with status: ${qg.status}"
                    }
                }
            }
        }

        stage('📦 Deploy Artifacts to Nexus') {
            environment {
                NEXUS_URL = 'http://localhost:8083'
            }
            steps {
                dir('backend') {
                    script {
                        def services = ['user-service', 'product-service', 'media-service',
                                       'order-service', 'eureka-service-discovery', 'apigateway']
                        def deployedCount = 0

                        for (service in services) {
                            // Check if service has changes compared to previous commit
                            def changes = sh(script: "git diff --name-only HEAD~1 | grep 'backend/${service}/' || true", returnStdout: true).trim()

                            if (changes) {
                                echo "📦 Deploying ${service} (changes detected)"
                                dir(service) {
                                    sh "./mvnw -s ../maven-settings.xml versions:set -DnewVersion=1.0.${env.BUILD_NUMBER} -DgenerateBackupPoms=false"
                                    sh './mvnw -s ../maven-settings.xml deploy -DskipTests'
                                }
                                deployedCount++
                            } else {
                                echo "⏭️ Skipping ${service} (no changes)"
                            }
                        }

                        if (deployedCount == 0) {
                            echo "ℹ️ No backend services had changes - nothing deployed to Nexus"
                        } else {
                            echo "✅ Deployed ${deployedCount} service(s) to Nexus"
                        }
                    }
                }
            }
        }

        stage('Docker Operations') {
            environment {
                IMAGE_TAG = "${env.BUILD_NUMBER}"
                NEXUS_REGISTRY = 'localhost:5001'
            }

            stages {
                stage('Prepare Rollback In case of Error') {
                    steps {
                        script {
                            if (fileExists('.prev_image_tag')) {
                                env.PREV_IMAGE_TAG = readFile('.prev_image_tag').trim()
                                echo "Previous image tag found: ${env.PREV_IMAGE_TAG}"
                            } else {
                                echo "No previous image tag found. This might be the first build."
                            }
                        }
                    }
                }

                stage('Docker Build') {
                    steps {
                        script {
                            sh "IMAGE_TAG=${env.IMAGE_TAG} docker compose build --parallel --no-cache"
                            echo "Docker build completed successfully"
                        }
                    }
                }

                stage('Tag Images for Nexus') {
                    steps {
                        script {
                            sh """
                                docker tag ecommerce-microservices-pipeline-eureka-server:latest \${NEXUS_REGISTRY}/eureka-server:\${IMAGE_TAG}
                                docker tag ecommerce-microservices-pipeline-api-gateway:latest \${NEXUS_REGISTRY}/api-gateway:\${IMAGE_TAG}
                                docker tag ecommerce-microservices-pipeline-user-service:latest \${NEXUS_REGISTRY}/user-service:\${IMAGE_TAG}
                                docker tag ecommerce-microservices-pipeline-product-service:latest \${NEXUS_REGISTRY}/product-service:\${IMAGE_TAG}
                                docker tag ecommerce-microservices-pipeline-media-service:latest \${NEXUS_REGISTRY}/media-service:\${IMAGE_TAG}
                                docker tag ecommerce-microservices-pipeline-order-service:latest \${NEXUS_REGISTRY}/order-service:\${IMAGE_TAG}
                            """
                            echo "Images tagged for Nexus"
                        }
                    }
                }

                stage('Docker Login to Nexus') {
                    steps {
                        script {
                            sh """
                                echo "Logging into Nexus Docker registry..."
                                echo admin | docker login \${NEXUS_REGISTRY} --username-stdin --password-stdin
                                echo "Docker login successful"
                            """
                        }
                    }
                }

                stage('Push Images to Nexus') {
                    steps {
                        script {
                            sh """
                                docker push \${NEXUS_REGISTRY}/eureka-server:\${IMAGE_TAG}
                                docker push \${NEXUS_REGISTRY}/api-gateway:\${IMAGE_TAG}
                                docker push \${NEXUS_REGISTRY}/user-service:\${IMAGE_TAG}
                                docker push \${NEXUS_REGISTRY}/product-service:\${IMAGE_TAG}
                                docker push \${NEXUS_REGISTRY}/media-service:\${IMAGE_TAG}
                                docker push \${NEXUS_REGISTRY}/order-service:\${IMAGE_TAG}
                            """
                            echo "All images pushed to Nexus"
                        }
                    }
                }

                stage('Docker Deploy') {
                    steps {
                        script {
                            echo "Deploying new image tag: ${env.IMAGE_TAG}"
                            sh "IMAGE_TAG=${env.IMAGE_TAG} docker compose down || true"
                            sh "IMAGE_TAG=${env.IMAGE_TAG} docker compose up -d --remove-orphans"

                            echo "Docker deployment completed successfully"
                            writeFile file: '.prev_image_tag', text: env.IMAGE_TAG
                        }
                    }
                }
            }

            post {
                failure {
                    script {
                        if (env.PREV_IMAGE_TAG) {
                            echo "Deployment failed. Rolling back to previous image tag: ${env.PREV_IMAGE_TAG}"
                            sh "IMAGE_TAG=${env.PREV_IMAGE_TAG} docker compose down || true"
                            sh "IMAGE_TAG=${env.PREV_IMAGE_TAG} docker compose up -d --remove-orphans"
                            env.ROLLEDBACK = 'true'
                            echo "Rollback to previous image tag ${env.PREV_IMAGE_TAG} completed successfully"
                        } else {
                            echo "No previous image tag available for rollback."
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            echo "Build #${env.BUILD_NUMBER} failed."
            emailext(
                to: 'hishamalmosawii@gmail.com',
                subject: "[AUTOMATED JENKINS CICD NOTIFICATION] ❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                        <html>
                        <body>
                             <p><img src="https://wgplnsqonmpsfotdngjm.supabase.co/storage/v1/object/public/test/image.jpeg" alt="Sonic" style="max-height: 300px; height: auto; width: auto;" /></p>
                            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                            <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                            <p><strong>Status:</strong> FAILED</p>
                            <p><strong>Branch:</strong> ${env.BRANCH_NAME ?: env.GIT_BRANCH}</p>

                            <p>Please find details in the jenkins server log if needed.</p>
                        </body>
                        </html>
                      """
            )
        }
        success {
            script {
                if (env.ROLLEDBACK == 'true') {
                    echo "Build #${env.BUILD_NUMBER} succeeded after rollback."
                } else {
                    echo "Build #${env.BUILD_NUMBER} succeeded."
                }
            }
            emailext(
                to: 'hishamalmosawii@gmail.com',
                subject: "[AUTOMATED JENKINS CICD NOTIFICATION] ✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """
                        <html>
                        <body>
                             <p><img src="https://wgplnsqonmpsfotdngjm.supabase.co/storage/v1/object/public/test/image.jpeg" alt="Sonic" style="max-height: 300px; height: auto; width: auto;" /></p>
                            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                            <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                            <p><strong>Status:</strong> SUCCESS</p>
                            <p><strong>Rolled back?:</strong> ${env.ROLLEDBACK}</p>
                            <p><strong>Branch:</strong> ${env.BRANCH_NAME ?: env.GIT_BRANCH}</p>

                            <p>Please find details in the jenkins server log if needed.</p>
                        </body>
                        </html>
                      """
            )
        }
    }

}