terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
  shared_credentials_files = ["~/.aws/credentials"]
}

variable "key_name" {
  type = string
  description = "Name of aws key_pair to assign to EC2"
}

variable "bot_credentials" {
  type = string
}

variable "bot_name" {
  type = string
}

variable "access_key_id" {
  type = string
}

variable "secret_access_key" {
  type = string
}

variable "journal_table_name" {
  type = string
  default = "englishbot_journal"
}

variable "snapshots_table_name" {
  type = string
  default = "englishbot_snapshots"
}

resource "aws_default_vpc" "default" {
}

resource "aws_security_group" "englishbot_sg" {
  name = "englishbot_security_group"
  description = "English Bot Security group"
  vpc_id      = aws_default_vpc.default.id

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
}

data "aws_ami" "amazon_linux_instance" {
  most_recent = true
  filter {
    name = "name"
    values = ["amzn2-ami-kernel-*-hvm-*-x86_64-*"]
  }
}

resource "aws_iam_role" "role" {
  name = "s3_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
      Effect = "Allow"
      Sid = ""
    }]
  })
}

resource "aws_iam_policy" "bot_server_policy" {
  name = "bot_server_role"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow"
	Resource = "*"
        Action = [
          "s3:*",
	  "s3-object-lambda:*"
	]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attachment" {
  role = aws_iam_role.role.name
  policy_arn = aws_iam_policy.bot_server_policy.arn
}

resource "aws_iam_instance_profile" "telegram_bot_profile" {
  name = "telegram_bot_profile"
  role = aws_iam_role.role.name
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

resource "aws_instance" "telegram_bot" {
  ami             = data.aws_ami.amazon_linux_instance.id
  instance_type   = "t2.micro"
  security_groups = [aws_security_group.englishbot_sg.name]
  iam_instance_profile = aws_iam_instance_profile.telegram_bot_profile.name
  key_name        = var.key_name

  user_data = templatefile("${path.module}/init_server.sh.tpl", {
    bot_credentials = var.bot_credentials,
    bot_name        = var.bot_name
    dynamodb_url    = "https://dynamodb.us-east-1.amazonaws.com"
    access_key_id   = var.access_key_id
    secret_access_key = var.secret_access_key
    journal_table_name   = var.journal_table_name
    snapshots_table_name = var.snapshots_table_name
  }  

  )

  tags = {
    Name = "EnglishBotServerTerraform"
  }
}
