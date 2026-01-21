resource "aws_iam_role" "alb_controller_irsa" {
  name = "alb-controller-irsa"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = module.eks.oidc_provider_arn
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringEquals = {
            "${replace(module.eks.oidc_provider, "https://", "")}:sub" = "system:serviceaccount:kube-system:aws-load-balancer-controller"
          }
        }
      }
    ]
  })
}

resource "aws_iam_policy" "alb_controller_policy" {
  name        = "AWSLoadBalancerControllerIAMPolicy"
  description = "IAM Policy for AWS Load Balancer Controller"

  policy = file("${path.module}/iam-policy.json")
}

resource "aws_iam_policy_attachment" "alb_controller" {
  name       = "alb-controller-attach"
  policy_arn = aws_iam_policy.alb_controller_policy.arn
  roles      = [aws_iam_role.alb_controller_irsa.name]
}

resource "kubernetes_service_account_v1" "alb_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.alb_controller_irsa.arn
    }
  }

  depends_on = [module.eks]
}
