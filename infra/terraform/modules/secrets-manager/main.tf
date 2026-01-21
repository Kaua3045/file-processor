resource "aws_secretsmanager_secret" "this" {
  name = "test/file-processor"
}

resource "aws_secretsmanager_secret_version" "this" {
  secret_id     = aws_secretsmanager_secret.this.id
  secret_string = jsonencode({
    DB_HOST       = var.db_host
    DB_PORT       = var.db_port
    DB_NAME       = var.db_name
    DB_USER       = var.db_user
    DB_PASSWORD   = var.db_password
    REDIS_HOST    = var.redis_host
    REDIS_PORT    = var.redis_port
    KAFKA_BROKERS = join(",", var.kafka_brokers)
  })
}
