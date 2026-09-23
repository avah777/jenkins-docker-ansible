pipeline {
    agent any

    environment {
        JAVA17_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'

        DOCKER_IMAGE = 'avah777/jenkins-docker-ansible'

        IMAGE_TAG = "${BUILD_NUMBER}"
        IMAGE_URI = "${DOCKER_IMAGE}:${BUILD_NUMBER}"

        ANSIBLE_DIR = 'ansible'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                sh '''
                    echo "===== JAVA ====="
                    java -version

                    echo "===== MAVEN ====="
                    mvn -version

                    echo "===== DOCKER ====="
                    docker --version

                    echo "===== ANSIBLE ====="
                    ansible --version
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    export JAVA_HOME="$JAVA17_HOME"
                    export PATH="$JAVA_HOME/bin:$PATH"

                    echo "===== RUNNING TESTS ====="

                    mvn clean test
                '''
            }
        }

        stage('Build') {
            steps {
                sh '''
                    export JAVA_HOME="$JAVA17_HOME"
                    export PATH="$JAVA_HOME/bin:$PATH"

                    echo "===== BUILDING APPLICATION ====="

                    mvn clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "===== BUILDING DOCKER IMAGE ====="

                    docker build \
                        -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                        -t ${DOCKER_IMAGE}:latest \
                        .
                '''
            }
        }

        stage('Docker Push') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                        echo "===== LOGIN TO DOCKER HUB ====="

                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        echo "===== PUSH VERSIONED IMAGE ====="

                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}

                        echo "===== PUSH LATEST IMAGE ====="

                        docker push ${DOCKER_IMAGE}:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Ansible Deploy') {
            steps {

                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ansible-ssh-key',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {

                    sh '''
                        echo "===== ANSIBLE VERSION ====="
                        ansible --version

                        echo "===== TEST ANSIBLE CONNECTION ====="

                        ansible \
                            -i ${ANSIBLE_DIR}/inventory.ini \
                            app_servers \
                            -m ping \
                            --private-key "$SSH_KEY" \
                            -u "$SSH_USER"
			    			-e 'ansible_ssh_common_args=-o StrictHostKeyChecking=no'

                        echo "===== DEPLOYING APPLICATION ====="

                        IMAGE_TAG=${BUILD_NUMBER} \
                        ansible-playbook \
                            -i ${ANSIBLE_DIR}/inventory.ini \
                            ${ANSIBLE_DIR}/deploy.yml \
                            --private-key "$SSH_KEY" \
                            -u "$SSH_USER"
		      	    		-e 'ansible_ssh_common_args=-o StrictHostKeyChecking=no'
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {

                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'ansible-ssh-key',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {

                    sh '''
                        echo "===== VERIFY APPLICATION SERVER ====="

                        ssh \
                            -i "$SSH_KEY" \
                            -o StrictHostKeyChecking=no \
                            "$SSH_USER@$(awk '/ansible_host=/{print $2}' ansible/inventory.ini | cut -d= -f2)" \
                            "docker ps"
                    '''
                }
            }
        }
    }

    post {

        success {
            echo '======================================'
            echo 'CI/CD PIPELINE SUCCESSFUL'
            echo '======================================'
        }

        failure {
            echo '======================================'
            echo 'CI/CD PIPELINE FAILED'
            echo '======================================'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}
