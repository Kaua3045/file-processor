resource "aws_msk_cluster" "this" {
  cluster_name           = "file-processor-msk"
  kafka_version          = "3.8.x"
  number_of_broker_nodes = 2

  broker_node_group_info {
    instance_type  = "kafka.m5.large"
    client_subnets = var.subnet_ids
    security_groups = [var.sg_id]
  }
}

resource "aws_msk_configuration" "this" {
  name      = "file-processor-msk-config"
  kafka_versions = ["3.8.x"]
  server_properties = "auto.create.topics.enable = true"
}
