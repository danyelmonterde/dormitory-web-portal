pipeline {
    agent any

    environment {
        REGISTRY = "192.168.0.250:5001"
        IMAGE_NAME = "dorm-portal"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push to Nexus Registry') {
            steps {
                sh "docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Deploy to K3s') {
            steps {
                withCredentials([file(credentialsId: 'k3s-kubeconfig', variable: 'KUBECONFIG')]) {
                    sh "kubectl --kubeconfig=${KUBECONFIG} set image deployment/${IMAGE_NAME}-deployment ${IMAGE_NAME}-container=${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} --namespace=default"
                }
            }
        }
    }

    post {
        success {
            echo "Deployment of ${IMAGE_NAME}:${IMAGE_TAG} succeeded!"
        }
        failure {
            echo "Pipeline failed. Check logs above."
        }
    }
}
