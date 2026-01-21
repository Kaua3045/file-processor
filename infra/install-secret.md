# Unicas duas coisas que nao funcionou foi o alb e o msk se conectar no spring fica dando disconect

# Instale o helm do external secret
```shell
helm install external-secrets external-secrets/external-secrets \
  -n external-secrets \
  --create-namespace \
  --set installCRDs=true \
  --set serviceAccount.create=true \
  --set serviceAccount.name=external-secrets-sa
```

# Anote o helm com o arn correto criado pelo terraform
```shell
kubectl annotate sa external-secrets-sa \
  -n external-secrets \
  eks.amazonaws.com/role-arn=arn:aws:iam::518216637660:role/external-secrets-irsa
```

# Atualize o kubectl deploy
```shell
kubectl patch deploy external-secrets \
  -n external-secrets \
  -p '{"spec":{"template":{"spec":{"serviceAccountName":"external-secrets-sa"}}}}'
```

# Reinice o Pod
```shell
kubectl rollout restart deploy external-secrets -n external-secrets
```
# Configure em values-prod e values o caminho correto da secret por exemplo test/file-processor

# Configurar AWS Load Balancer Controller
## Pegamos o oidc
```shell
aws eks describe-cluster --name file-processor-eks --query "cluster.identity.oidc.issuer" --output text
```
## Baixamos as permisões
```shell
curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json
```
## Criamos a policy
```shell
aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam-policy.json
```
## Criamos o trust-policy.json
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Federated": "arn:aws:iam::518216637660:oidc-provider/oidc.eks.us-east-1.amazonaws.com/id/ED43402FBD6D3181FBDE1F0EF2C84B64" },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "OIDC_PROVIDER_AQUI:sub": "system:serviceaccount:kube-system:aws-load-balancer-controller"
        }
      }
    }
  ]
}
```
## Criamos a role
```shell
aws iam create-role \
    --role-name ALBControllerRole \
    --assume-role-policy-document file://trust-policy.json
```
## Fazemos o attach
```shell
aws iam attach-role-policy \
    --role-name ALBControllerRole \
    --policy-arn arn:aws:iam::518216637660:policy/AWSLoadBalancerControllerIAMPolicy
```
## Kubectl apply do service account
```shell
kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: aws-load-balancer-controller
  namespace: kube-system
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::518216637660:role/ALBControllerRole
EOF
```
## Adicionamos o repo do eks atualizamos
```shell
helm repo add eks https://aws.github.io/eks-charts
helm repo update
```
## Pegamos a vpc
```shell
aws eks describe-cluster --name file-processor-eks --query "cluster.resourcesVpcConfig.vpcId" --output text
```
## Baixamos agora o controller
```shell
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=file-processor-eks \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set region=us-east-1 \
    --set vpcId=vpc-02eef110bf85ae201
```
# Se estiver tudo ok vai ficar ready 2/2
```shell
kubectl get deployment -n kube-system aws-load-balancer-controller
```

# Outra forma de instalar o load balancer caso use o terraform pra criar o service account
```shell
kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=main"

helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<EKS_CLUSTER_NAME> \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set region=<AWS_REGION> \
  --set vpcId=<VPC_ID>
```
```shell
kubectl edit deployment aws-load-balancer-controller -n kube-system

spec:
  nodeSelector:
    role: alb-controller
```