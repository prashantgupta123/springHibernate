def DOCKER_IMAGE_NAME
pipeline {
    agent any
    parameters {
        string(name: 'GIT_BRANCH', defaultValue: 'qa', description: 'Git Branch Name')
        string(name: 'ENV', defaultValue: 'prod', description: 'Environment Name')
    }
    environment {
        REGION_NAME = "ap-south-1"
        ECS_IMAGE_TAG = "latest"
        REPOSITORY_NAME = "spring-hibernate"
        IMAGE_NAME = "123456789.dkr.ecr.${env.REGION_NAME}.amazonaws.com/${env.REPOSITORY_NAME}"
        ECS_IMAGE_NAME = "${env.IMAGE_NAME}:${env.ECS_IMAGE_TAG}"
        PROFILE_NAME = "spring-hibernate"
        CLUSTER_NAME = "spring-hibernate-cluster"
        TASK_NAME = "spring-hibernate-task"
        SERVICE_NAME = "spring-hibernate-service"
        GIT_CREDENTIAL = "3cf0edc8-9c1d-4ea3-b47c-asd45csd26xs"
        GIT_URL = "git@github.com:prashantgupta123/springHibernate.git"
    }
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        disableResume()
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Git Pull') {
            steps {
                script {
                    git branch: "${params.GIT_BRANCH}",
                            credentialsId: "${env.GIT_CREDENTIAL}",
                            poll: false,
                            url: "${env.GIT_URL}"
                    def COMMIT_ID = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
                    DOCKER_IMAGE_NAME = "${env.IMAGE_NAME}:${params.ENV}-${params.GIT_BRANCH}-${COMMIT_ID}"
                }
            }
        }
        stage('Docker Image Push') {
            steps {
                script {
                    sh """
                        echo "Docker Image Push"
                        /usr/bin/aws ecr get-login --no-include-email --region ${env.REGION_NAME} --profile ${env.PROFILE_NAME} | sh
                        docker rmi -f ${DOCKER_IMAGE_NAME}
                        docker build -t ${DOCKER_IMAGE_NAME} .
                        docker tag ${DOCKER_IMAGE_NAME} ${env.ECS_IMAGE_NAME}
                        docker push ${env.ECS_IMAGE_NAME}
                        echo ${DOCKER_IMAGE_NAME}
                    """
                }
            }
        }
        stage('Registering New Task') {
            steps {
                script {
                    sh """
                        NEW_DOCKER_IMAGE="${env.ECS_IMAGE_NAME}"
                        OLD_TASK_DEF=\$(/usr/bin/aws ecs describe-task-definition \
                            --task-definition ${env.TASK_NAME} \
                            --output json \
                            --region ${env.REGION_NAME} \
                            --profile ${env.PROFILE_NAME})
                        NEW_TASK_DEF=\$(echo \$OLD_TASK_DEF | jq --arg NDI \$NEW_DOCKER_IMAGE '.taskDefinition.containerDefinitions[0].image=\$NDI')
                        FINAL_TASK=\$(echo \$NEW_TASK_DEF | jq '.taskDefinition|{family: .family, volumes: .volumes, containerDefinitions: .containerDefinitions, placementConstraints: .placementConstraints}')
                        TASK_OUTPUT=\$(/usr/bin/aws ecs register-task-definition \
                            --family ${env.TASK_NAME} \
                            --cli-input-json "\$(echo \$FINAL_TASK)"  \
                            --region ${env.REGION_NAME} \
                            --profile ${env.PROFILE_NAME})
                    """
                }
            }
        }
        stage('Updating Service') {
            steps {
                script {
                    sh """
                        /usr/bin/aws ecs update-service \
                            --service ${env.SERVICE_NAME}  \
                            --task-definition ${env.TASK_NAME} \
                            --cluster ${env.CLUSTER_NAME}  \
                            --region ${env.REGION_NAME} \
                            --profile ${env.PROFILE_NAME}
                            && aws ecs wait services-stable \
                            --cluster "${env.CLUSTER_NAME}" \
                            --services "${env.SERVICE_NAME}"
                        
                        if [ \$? -ne 0 ]
                        then
                            echo "Error in deployment"
                        else
                            echo "Application has been deployed successfully !!"
                            exit 1
                        fi
                    """
                }
            }
        }
    }
}
