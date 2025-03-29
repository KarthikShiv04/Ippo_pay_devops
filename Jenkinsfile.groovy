pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = 'your-aws-account-id'
        ECR_REPOSITORY = 'your-ecr-repo-name'
        IMAGE_TAG = "ippo-pay-react-app-${env.BUILD_NUMBER}"
    }

    parameters {
        booleanParam(name: 'DEPLOY_PROD', defaultValue: false, description: 'Deploy to Production? (true for prod, false for staging)')
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/KarthikShiv04/Ippo_pay_devops.git'
            }
        }

        stage('Install Dependencies') {
            steps {
                script {
                    sh 'npm install'
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    sh 'npm test'
                }
            }
        }

        stage('Build Application') {
            steps {
                script {
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    withAWS(region: env.AWS_REGION, credentials: 'aws-credentials-id') {
                        sh "aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
                        sh "docker build -t $ECR_REPOSITORY:$IMAGE_TAG ."
                        sh "docker tag $ECR_REPOSITORY:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG"
                        sh "docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def environmentName = params.DEPLOY_PROD ? "production" : "staging"
                    def port = params.DEPLOY_PROD ? "80:80" : "3000:80"

                    sh """
                        docker run -d --name ippo-pay-react-app-${environmentName} \\
                        -e NODE_ENV=${environmentName} \\
                        -p ${port} \\
                        $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$IMAGE_TAG
                    """
                }
            }
        }
    }
}
