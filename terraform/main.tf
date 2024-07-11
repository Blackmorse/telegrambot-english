terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
  shared_credentials_files = ["~/.aws/credentials"]
}


variable "journal_table_name" {
  type = string
  default = "englishbot_journal"
}

variable "snapshots_table_name" {
  type = string
  default = "englishbot_snapshots"
}


resource "aws_dynamodb_table" "journal_table" {
  name         =var.journal_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "par"
  range_key    = "num"

  attribute {
    name = "par"
    type = "S"
  }

  attribute {
    name = "num"
    type = "N"
  }
}

resource "aws_dynamodb_table" "snapshots_table" {
  name         = var.snapshots_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "par"
  range_key    = "seq"

  attribute {
    name = "par"
    type = "S"
  }

  attribute {
    name = "seq"
    type = "N"
  }

  attribute {
    name = "ts"
    type = "N"
  }

  local_secondary_index {
    name      = "ts-idx"
    projection_type = "ALL"
    range_key       = "ts"
  }
}
