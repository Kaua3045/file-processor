variable "region" {
  description = "AWS region"
  default     = "us-east-1"
}

variable "db_name" {
  description = "PostgreSQL database name"
  default     = "file_processor"
}

variable "db_user" {
  description = "PostgreSQL username"
  default     = "fileuser"
}

variable "db_password" {
  description = "PostgreSQL password"
  default     = "ChangeMe123!"
  sensitive   = true
}