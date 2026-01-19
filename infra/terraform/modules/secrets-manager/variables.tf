variable "db_host" {}
variable "db_port" {}
variable "db_name" {}
variable "db_user" {}
variable "db_password" { sensitive = true }
variable "redis_host" {}
variable "redis_port" {}
variable "kafka_brokers" { type = list(string) }
