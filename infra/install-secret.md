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