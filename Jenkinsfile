pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-southeast-1'
        APP_NAME = 'dorm-portal'
        // EC2 IPs configured in Jenkins Credentials/Environment
        DEV_IP = credentials('ec2-dev-ip')
        UAT_IP = credentials('ec2-uat-ip')
        PROD_IP = credentials('ec2-prod-ip')
        SSH_CRED = 'ec2-ssh-key' 
        // Add homebrew and podman paths to PATH for the local Jenkins agent
        PATH = "/opt/homebrew/bin:/opt/podman/bin:${env.PATH}"
    }

    stages {
        stage('Checkout & Unit Tests') {
            steps {
                echo 'Checking out code and running unit tests...'
                dir('backend') {
                    sh 'mvn clean test'
                }
                dir('frontend') {
                    sh 'npm install --legacy-peer-deps'
                    sh 'ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Local Podman Integration Test') {
            steps {
                echo 'Building and verifying container locally with Podman...'
                sh 'podman build --platform linux/amd64 -t ${APP_NAME}:test-build .'
                // Run a quick verification command to ensure the container starts
                sh 'podman run --rm ${APP_NAME}:test-build java -version'
            }
        }

        stage('Deploy to Develop') {
            when {
                anyOf {
                    branch 'develop'
                    expression { env.GIT_BRANCH == 'origin/develop' || env.GIT_BRANCH == 'develop' || env.GIT_BRANCH == 'refs/heads/develop' }
                }
            }
            steps {
                echo 'Deploying to Develop Environment (EC2: 8GB RAM)...'
                script {
                    deployToEC2(DEV_IP, 'dev')
                }
            }
        }

        stage('Deploy to UAT') {
            when {
                anyOf {
                    branch 'release'
                    expression { env.GIT_BRANCH == 'origin/release' || env.GIT_BRANCH == 'release' || env.GIT_BRANCH == 'refs/heads/release' }
                }
            }
            steps {
                echo 'Deploying to UAT Environment (EC2: 8GB RAM)...'
                script {
                    deployToEC2(UAT_IP, 'uat')
                }
            }
        }

        stage('Deploy to Production') {
            when {
                anyOf {
                    branch 'main'
                    expression { env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'refs/heads/main' }
                }
            }
            steps {
                echo 'Deploying to Production Environment (EC2: 8GB RAM)...'
                script {
                    deployToEC2(PROD_IP, 'prod')
                }
            }
        }
    }
}

// Reusable deployment function
def deployToEC2(serverIp, envPrefix) {
    sshagent([SSH_CRED]) {
        // 1. Build and export Podman image
        sh "podman build --platform linux/amd64 -t ${APP_NAME}:${envPrefix} ."
        sh "podman save ${APP_NAME}:${envPrefix} | bzip2 > ${APP_NAME}-${envPrefix}.tar.bz2"
        
        // 2. Transfer image to EC2
        sh "scp -o StrictHostKeyChecking=no ${APP_NAME}-${envPrefix}.tar.bz2 ec2-user@${serverIp}:~/"
        
        // 3. Execute remote commands: Load image, fetch secrets, run container
        sh """
        ssh -o StrictHostKeyChecking=no ec2-user@${serverIp} '
            # Create user-defined network if it doesn't exist
            docker network create dorm-network || true
            
            # Start MySQL container if not already running on the network
            if ! docker ps --filter "name=dorm-mysql" --format "{{.Names}}" | grep -q "^dorm-mysql\$"; then
                docker rm -f dorm-mysql || true
                docker run -d --name dorm-mysql \\
                    --network dorm-network \\
                    -p 3306:3306 \\
                    -e MYSQL_ROOT_PASSWORD=password \\
                    -e MYSQL_DATABASE=dormitory_local \\
                    --restart unless-stopped \\
                    mysql:8.0
                
                # Give MySQL some time to initialize
                sleep 15
            fi
            
            # Stop existing container
            docker stop ${APP_NAME}-${envPrefix} || true
            docker rm ${APP_NAME}-${envPrefix} || true
            
            # Load new image
            bunzip2 -c ${APP_NAME}-${envPrefix}.tar.bz2 | docker load
            docker tag localhost/${APP_NAME}:${envPrefix} ${APP_NAME}:${envPrefix} || true
            
            # Fetch secrets securely from AWS Secrets Manager using IAM role attached to EC2
            aws secretsmanager get-secret-value --secret-id ${envPrefix}-dorm-secrets --query SecretString --output text > .env.${envPrefix}
            
            # Dynamically route DB connection to the dorm-mysql container instead of localhost
            sed -i "s/localhost:3306/dorm-mysql:3306/g" .env.${envPrefix}
            
            # Run new container attached to the network
            docker run -d --name ${APP_NAME}-${envPrefix} \\
                --network dorm-network \\
                --env-file .env.${envPrefix} \\
                -p 80:8080 \\
                --restart unless-stopped \\
                ${APP_NAME}:${envPrefix}
                
            # Clean up secrets file immediately from disk
            rm .env.${envPrefix}
        '
        """
    }
}
