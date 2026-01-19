resource "aws_db_instance" "this" {
  identifier          = "file-processor-db"
  engine              = "postgres"
  instance_class      = "db.t3.micro"
  allocated_storage   = 20
  username            = var.db_user
  password            = var.db_password
  db_name             = var.db_name
  skip_final_snapshot = true
  publicly_accessible = true
  vpc_security_group_ids = [var.sg_id]
  db_subnet_group_name = aws_db_subnet_group.this.name
}

resource "aws_db_subnet_group" "this" {
  name       = "file-processor-db-subnet-group"
  subnet_ids = var.subnet_ids
}
