## How to start the project

1. Start minikube

        minikube start

1. Build and deploy maven backend project (in `backend` folder)

        ./k8sBuildAndDeploy.sh

    :information_source: Docker daemon in switched to the one in minikube, so that kubernetes can see the image.

1. Apply all kubernetes objects (in `env` folder)

        kubectl apply -f kubernetes.yaml

1. Expose load balancer service endpoint

        minikube service mobilecs-service --url
    
    :information_source: Mind the URL and use it in requests below.

1. Generate test CDR Data Records, read records and calculate total data used for agreement/year/month.

        curl http://127.0.0.1:63277/api/data-records/generate
        
        curl http://127.0.0.1:63277/api/data-records/0b12c601-9287-3f5c-a78c-df508fe0f889/2020/01

        curl http://127.0.0.1:63277/api/data-records/0b12c601-9287-3f5c-a78c-df508fe0f889/2020/01/total-data-used        

