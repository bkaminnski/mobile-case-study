# Run the project

## Kubernetes

1. Install and start minikube, https://minikube.sigs.k8s.io/docs/start/, e.g.:

        minikube start --driver=hyperkit --cpus=3 --memory=8g

1. Build and deploy maven backend project (in `backend` folder)

        ./k8s-build-and-deploy.sh

    :information_source: Docker daemon switched to minikube, so that kubernetes can access mobilecs project images.

1. Apply all kubernetes objects

        kubectl apply -f kubernetes.yaml

1. Start Kafka consumer for expected data records aggregated in Flink

        kubectl exec `kubectl get pods -l app=kafka -o name` -- bash  -c "kafka-console-consumer --topic data-records-aggregates --from-beginning --bootstrap-server kafka-service:29092 --property print.timestamp=true"

1. Generate test CDR Data Records and agreements

        curl `minikube service backend-service --url`/api/agreements/generate
        curl `minikube service backend-service --url`/api/incoming-data-records/generate

1. Other commands for further insight
    - Kafka consumer for agreements generated in backend service
        
            kubectl exec `kubectl get pods -l app=kafka -o name` -- bash  -c "kafka-console-consumer --topic agreements --from-beginning --bootstrap-server kafka-service:29092 --property print.timestamp=true"
        
    - Kafka consumer for data records imported in Flink
        
            kubectl exec `kubectl get pods -l app=kafka -o name` -- bash  -c "kafka-console-consumer --topic incoming-data-records --from-beginning --bootstrap-server kafka-service:29092 --property print.timestamp=true"
    
    - Open bash for backend service
    
            kubectl exec -it `kubectl get pods -l app=backend -o name` -- bash
        
1. Useful kubernetes commands (read about stern [here](https://github.com/burrsutter/9stepsawesome/blob/3ddeead8b5cd5841760f2a4beb90eeae35a8a4b1/3_logs.adoc))

        minikube dashboard
        kubectl get pods --show-labels -w
        watch -n 0.1 kubectl get pods --show-labels
        stern backend

