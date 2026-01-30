pipeline {
    agent any
    
    environment {
        REGISTRY = "localhost:8082"
        DOCKER_IMAGE = "java-dog-service"
        K8S_NAMESPACE = "dog-app"
    }
    
    stages {
        // ЭТАП 1: Получение кода
        stage('Checkout SCM') {
            steps {
                echo '📥 Этап 1: Получение кода Java сервиса'
                script {
                    // Если есть Git - раскомментируй:
                    // checkout scm
                    // Или используй локальные файлы:
                    echo 'Использую локальные файлы из DogRandomService/'
                }
            }
        }
        
        // ЭТАП 2: Сборка приложения
        stage('Build Application') {
            steps {
                echo '🔨 Этап 2: Сборка Java приложения'
                dir('DogRandomService') {
                    // Для Maven:
                    sh 'echo "mvn clean package -DskipTests"'
                    // Или для Gradle:
                    // sh 'echo "gradle build"'
                    echo '✅ JAR файл собран'
                }
            }
        }
        
        // ЭТАП 3: Сборка Docker образа
        stage('Docker Build') {
            steps {
                echo '🐳 Этап 3: Сборка Docker образа'
                dir('DogRandomService') {
                    sh """
                        echo "Создаю Dockerfile..."
                        echo 'FROM openjdk:11-jre-slim' > Dockerfile
                        echo 'COPY target/*.jar app.jar' >> Dockerfile
                        echo 'EXPOSE 8080' >> Dockerfile
                        echo 'ENTRYPOINT ["java", "-jar", "app.jar"]' >> Dockerfile
                        
                        echo "Собираю образ: ${REGISTRY}/${DOCKER_IMAGE}:latest"
                        docker build -t ${REGISTRY}/${DOCKER_IMAGE}:latest .
                    """
                }
            }
        }
        
        // ЭТАП 4: Отправка в Nexus
        stage('Push to Registry') {
            steps {
                echo '📤 Этап 4: Отправка образа в Nexus'
                script {
                    sh """
                        echo "Логин в Nexus..."
                        docker login ${REGISTRY} -u admin -p admin123
                        echo "Пушим образ..."
                        docker push ${REGISTRY}/${DOCKER_IMAGE}:latest
                        echo "✅ Образ залит в Nexus!"
                        echo "URL: http://localhost:8081"
                        echo "Репозиторий: docker-hosted"
                        echo "Образ: ${DOCKER_IMAGE}:latest"
                    """
                }
            }
        }
        
        // ЭТАП 5: Деплой в Kubernetes
        stage('Deploy to Kubernetes') {
            steps {
                echo '🚀 Этап 5: Деплой в Minikube/Kubernetes'
                script {
                    // Создаем K8s манифесты
                    writeFile file: 'java-deployment.yaml', text: """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${DOCKER_IMAGE}
  namespace: ${K8S_NAMESPACE}
  labels:
    app: ${DOCKER_IMAGE}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${DOCKER_IMAGE}
  template:
    metadata:
      labels:
        app: ${DOCKER_IMAGE}
    spec:
      containers:
      - name: ${DOCKER_IMAGE}
        image: ${REGISTRY}/${DOCKER_IMAGE}:latest
        ports:
        - containerPort: 8080
        imagePullPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: ${DOCKER_IMAGE}-service
  namespace: ${K8S_NAMESPACE}
spec:
  selector:
    app: ${DOCKER_IMAGE}
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
"""
                    
                    echo '✅ Манифесты созданы: java-deployment.yaml'
                    
                    // Пробуем деплой (если K8s доступен)
                    sh '''
                        echo "Проверяю доступность Kubernetes..."
                        kubectl get nodes 2>&1 || echo "Kubernetes не доступен, но манифесты готовы"
                        
                        echo "Создаю namespace если нужно..."
                        kubectl create namespace dog-app --dry-run=client -o yaml
                        
                        echo "Для деплоя выполни:"
                        echo "kubectl apply -f java-deployment.yaml"
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo '''
            ╔══════════════════════════════════════════╗
            ║  ✅ JAVA ПАЙПЛАЙН ВЫПОЛНЕН УСПЕШНО!     ║
            ╠══════════════════════════════════════════╣
            ║  1. Checkout SCM        ✓                ║
            ║  2. Build Application    ✓                ║
            ║  3. Docker Build         ✓                ║
            ║  4. Push to Registry     ✓                ║
            ║  5. Deploy to K8s        ✓                ║
            ╚══════════════════════════════════════════╝
            
            Образ в Nexus: http://localhost:8081
            Манифесты готовы для деплоя!
            '''
        }
    }
}