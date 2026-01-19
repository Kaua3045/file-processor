output "rds_endpoint" {
  value = module.rds.endpoint
}

output "rds_port" {
  value = module.rds.port
}

output "redis_endpoint" {
  value = module.redis.endpoint
}

output "redis_port" {
  value = module.redis.port
}

output "msk_bootstrap_brokers" {
  value = module.msk.bootstrap_brokers
}

output "secrets_arn" {
  value = module.secrets_manager.secret_arn
}
output "eks_cluster_name" { value = module.eks.cluster_id }
output "eks_cluster_endpoint" { value = module.eks.cluster_endpoint }
output "eks_cluster_security_group_id" { value = module.eks.cluster_security_group_id }
