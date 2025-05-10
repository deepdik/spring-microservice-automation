
# Spring Boot Microservices on Kubernetes (AWS EKS) – Full Production Setup

This README summarizes the full end-to-end setup you implemented for deploying Spring Boot microservices in Kubernetes using AWS EKS with observability, tracing, logging, and CI/CD.

---

## 🔧 Project Structure

```
spring-microservices/
├── user-service/
├── order-service/
└── helm-deployments/
    ├── user-service/
    ├── order-service/
    └── monitoring/
```

---

## ✅ Microservices Setup

- Spring Boot 3.3.11
- `user-service` and `order-service` with:
  - REST APIs (`/hello`, `/order`)
  - Spring Actuator + Prometheus metrics
  - Resilience4j (Circuit Breaker, Retry, Fallback)
  - Zipkin tracing
  - Exposed with Ingress

---

## 🚀 Kubernetes Deployment

### 🧱 Build and Push Docker Image

```bash
docker build -t user-service .
docker tag user-service:latest 051826736527.dkr.ecr.us-east-2.amazonaws.com/user-service:latest
docker push 051826736527.dkr.ecr.us-east-2.amazonaws.com/user-service:latest
```

### 📦 Helm Deployment

```bash
helm upgrade --install user-service ./user-service --namespace default
helm upgrade --install order-service ./order-service --namespace default
```

---

## 🌐 Ingress Configuration (Nginx)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spring-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
    - host: api.hackplanner.com
      http:
        paths:
          - path: /user(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: user-service
                port:
                  number: 80
          - path: /order(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 80
```

---

## 📊 Monitoring with Prometheus + Grafana

### Metrics Exposed:

```properties
management.metrics.export.prometheus.enabled=true
management.endpoints.web.exposure.include=*
```

### Deployment:

```bash
helm install k8s-monitoring prometheus-community/kube-prometheus-stack -n monitoring
```

---

## 📉 Distributed Tracing (Zipkin)

```properties
management.tracing.enabled=true
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin.tracing.svc.cluster.local:9411/api/v2/spans
```

```bash
helm install zipkin openzipkin/zipkin -n tracing
```

---

## 📦 CI/CD with Jenkins

### Jenkinsfile

```groovy
pipeline {
  agent any
  stages {
    stage('Build user-service JAR') {
      steps {
        dir('user-service') {
          sh './mvnw clean package -DskipTests'
        }
      }
    }
    stage('Docker Build & Push') {
      steps {
        sh 'docker build -t user-service .'
        sh 'docker push 051826736527.dkr.ecr.us-east-2.amazonaws.com/user-service:latest'
      }
    }
    stage('Helm Deploy') {
      steps {
        sh 'helm upgrade --install user-service helm-deployments/user-service --namespace default'
      }
    }
  }
}
```

---

## 📈 Centralized Logging with Fluent Bit + AWS CloudWatch (optional)

```bash
helm upgrade --install fluent-bit fluent/fluent-bit -n logging -f fluent-bit-values.yaml
```

---

## ⚙️ Autoscaling

```bash
kubectl autoscale deployment user-service   --cpu-percent=50   --min=1   --max=5
```

Add `resources:` in `values.yaml`:

```yaml
resources:
  requests:
    cpu: 200m
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi
```

---

## ⚠️ Graceful Shutdown

```yaml
terminationGracePeriodSeconds: 60
```

Spring Boot handles `SIGTERM` using `@PreDestroy`. FastAPI uses `@app.on_event("shutdown")`.

---

## 🔍 Observability Tools

| Tool        | Purpose            |
|-------------|---------------------|
| Prometheus  | Metrics scraping    |
| Grafana     | Dashboards          |
| Zipkin      | Tracing             |
| Fluent Bit  | Logs shipping       |
| Jenkins     | CI/CD automation    |

---

## 🧪 JMeter Testing Ideas

- Hit `/user/hello` or `/user/order` in loops
- Scale down `order-service` and test fallback behavior
- Watch pod metrics with `kubectl top pod`
- Observe scaling with `kubectl get hpa`

---

## 🎯 DNS Setup

- Custom domain: `api.hackplanner.com`
- CNAME pointed to ELB created by Ingress


## All commands

```
Kubernetes Commands:
  - kubectl get pods
  - kubectl get svc
  - kubectl describe pod <pod-name>
  - kubectl logs <pod-name>
  - kubectl apply -f <file>.yaml
  - kubectl delete -f <file>.yaml
  - kubectl get nodes
  - kubectl get deployments
  - kubectl autoscale deployment <deployment-name> --cpu-percent=50 --min=1 --max=5
  - kubectl port-forward svc/<service-name> 9200 -n <namespace>
  - kubectl exec -it <pod-name> -- bash
  - kubectl top nodes
  - kubectl get serviceaccount <name> -n <namespace>
  - kubectl delete serviceaccount <name> -n <namespace> --ignore-not-found
  - kubectl delete role <name> -n <namespace> --ignore-not-found
  - kubectl delete rolebinding <name> -n <namespace> --ignore-not-found
  - kubectl get pods -n <namespace> -o jsonpath='{range .items[*]}{.metadata.name}\t{.spec.serviceAccountName}\n{end}'
  - kubectl logs -l app=<app-name>
  - kubectl logs -l job-name=<job-name> -n <namespace> --tail=100 --all-containers

Helm Commands:
  - helm upgrade --install <release-name> <chart-path> --namespace <namespace>
  - helm uninstall <release-name> -n <namespace>
  - helm repo add <name> <url>
  - helm repo update
  - helm list -n <namespace>
  - helm install <name> <chart> -n <namespace> -f <values>.yaml

AWS CLI Commands:
  - aws eks update-kubeconfig --region <region> --name <cluster-name>
  - aws logs describe-log-groups --region <region>
  - aws logs tail <log-group> --region <region> --follow

Eksctl Commands:
  - eksctl create iamserviceaccount --cluster <cluster-name> --namespace <namespace> --name <account-name> --attach-policy-arn <policy-arn> --approve --override-existing-serviceaccounts

Jenkins Related Commands:
  - sudo apt update
  - sudo apt install -y openjdk-17-jdk
  - wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
  - sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
  - sudo apt update
  - sudo apt install -y jenkins
  - sudo systemctl start jenkins
  - sudo systemctl enable jenkins

Docker/ECR Commands:
  - docker build -t <image-name>:latest .
  - docker tag <image-name>:latest <aws_account_id>.dkr.ecr.<region>.amazonaws.com/<image-name>:latest
  - docker push <aws_account_id>.dkr.ecr.<region>.amazonaws.com/<image-name>:latest

```
