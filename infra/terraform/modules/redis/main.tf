resource "aws_elasticache_subnet_group" "this" {
  name       = "file-processor-redis-subnet-group"
  subnet_ids = var.subnet_ids
}

resource "aws_elasticache_cluster" "this" {
  cluster_id           = "file-processor-redis"
  engine               = "redis"
  engine_version = "7.0"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  port                 = 6379
  parameter_group_name = "default.redis7"
  subnet_group_name    = aws_elasticache_subnet_group.this.name
  security_group_ids   = [var.sg_id]
}
