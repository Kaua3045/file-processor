provider "aws" {
  region = var.region
}

provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = [
      "eks", "get-token",
      "--cluster-name", module.eks.cluster_name,
      "--region", var.region
    ]
  }
}

data "aws_availability_zones" "available" {}

# ===== VPC =====
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name                 = "file-processor-vpc"
  cidr                 = "10.0.0.0/16"
  azs                  = slice(data.aws_availability_zones.available.names, 0, 2)
  private_subnets      = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets       = ["10.0.101.0/24", "10.0.102.0/24"]
  enable_nat_gateway   = true
  single_nat_gateway   = true
  tags = { "Name" = "file-processor-vpc" }

  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"            # ALB público
    "kubernetes.io/cluster/file-processor-eks" = "shared"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = "1"   # ALB interno
    "kubernetes.io/cluster/file-processor-eks" = "shared"
  }

  map_public_ip_on_launch = true
}

# ===== EKS Cluster =====
module "eks" {
  source          = "terraform-aws-modules/eks/aws"
  version         = "~> 20.0"
  cluster_name    = "file-processor-eks"
  cluster_version = "1.30"

  # rede
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # habilita IRSA para External Secrets
  enable_irsa = true

  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  cluster_endpoint_public_access_cidrs = [
    "0.0.0.0/0"
  ]

  access_entries = {
    admin = {
      principal_arn = "arn:aws:iam::518216637660:user/kaua-dev"
      policy_associations = {
        admin = {
          policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
          access_scope = {
            type = "cluster"
          }
        }
      }
    }
  }

  # Alb nodes public
  eks_managed_node_groups = {
    alb_nodes = {
      desired_capacity = 1
      max_capacity     = 1
      min_capacity     = 1
      instance_type    = "t3.medium"
      subnet_ids       = module.vpc.public_subnets
      labels = { "role" = "alb-controller" }
    }
    app_nodes = {
      desired_capacity = 2
      max_capacity     = 4
      min_capacity     = 2
      instance_type    = "t3.medium"
      subnet_ids       = module.vpc.private_subnets
      labels = {
        role = "app"
      }
    }
  }

  # Defaults para todos os node groups
  eks_managed_node_group_defaults = {
    ami_type        = "AL2_x86_64"
    disk_size       = 20
    force_update_version = true
    labels = {
      env = "prod"
    }
    additional_security_group_ids = [
      aws_security_group.eks.id
    ]
  }
}

# ===== IAM Role para External Secrets (IRSA) =====
resource "aws_iam_role" "external_secrets_sa" {
  name = "file-processor-external-secrets-sa"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "eks.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}


resource "aws_iam_policy" "external_secrets" {
  name = "external-secrets-policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret",
          "secretsmanager:ListSecrets"
        ],
        Resource = "*"
      }
    ]
  })
}

module "external_secrets_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "external-secrets-irsa"

  oidc_providers = {
    eks = {
      provider_arn = module.eks.oidc_provider_arn
      namespace_service_accounts = [
        "external-secrets:external-secrets-sa"
      ]
    }
  }

  role_policy_arns = {
    secretsmanager = aws_iam_policy.external_secrets.arn
  }
}

# ===== Security Groups =====
resource "aws_security_group" "eks" {
  name        = "file-processor-eks-sg"
  description = "SG for EKS cluster"
  vpc_id      = module.vpc.vpc_id
}

resource "aws_security_group" "rds" {
  name   = "file-processor-rds-sg"
  vpc_id = module.vpc.vpc_id
}

resource "aws_security_group" "redis" {
  name   = "file-processor-redis-sg"
  vpc_id = module.vpc.vpc_id
}

resource "aws_security_group" "msk" {
  name   = "file-processor-msk-sg"
  vpc_id = module.vpc.vpc_id
}

resource "aws_security_group_rule" "eks_to_rds" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = module.eks.node_security_group_id
}

resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = aws_security_group.redis.id
  source_security_group_id = module.eks.node_security_group_id
}

resource "aws_security_group_rule" "eks_to_msk" {
  type                     = "ingress"
  from_port                = 9094
  to_port                  = 9094
  protocol                 = "tcp"
  security_group_id        = aws_security_group.msk.id
  source_security_group_id = module.eks.node_security_group_id
}

resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  security_group_id = aws_security_group.rds.id
  cidr_blocks       = ["0.0.0.0/0"]
}

# ===== IAM Role for External Secrets =====
resource "aws_iam_role" "eks_external_secrets" {
  name = "file-processor-es-sa"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = { Service = "eks.amazonaws.com" },
      Action = "sts:AssumeRole"
    }]
  })
}

# ===== Módulos =====
module "rds" {
  source     = "./modules/rds"
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
  sg_id      = aws_security_group.rds.id
  db_name    = var.db_name
  db_user    = var.db_user
  db_password= var.db_password
}

module "redis" {
  source     = "./modules/redis"
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
  sg_id      = aws_security_group.redis.id
}

module "msk" {
  source     = "./modules/msk"
  subnet_ids = module.vpc.private_subnets
  sg_id      = aws_security_group.eks.id
}

module "secrets_manager" {
  source        = "./modules/secrets-manager"
  db_host       = module.rds.endpoint
  db_port       = module.rds.port
  db_name       = var.db_name
  db_user       = var.db_user
  db_password   = var.db_password
  redis_host    = module.redis.endpoint
  redis_port    = module.redis.port
  kafka_brokers = split(",", module.msk.bootstrap_brokers)
}
