#!/bin/bash

sudo amazon-linux-extras install java-openjdk11 -y

export AWS_ACCESS_KEY_ID="${access_key_id}"
export AWS_SECRET_ACCESS_KEY="${secret_access_key}"
export DYNAMODB_ENDPOINT="${dynamodb_url}"
export JOURNAL_TABLE_NAME="${journal_table_name}"
export SNAPSHOTS_TABLE_NAME="${snapshots_table_name}"

aws s3 cp s3://englishbot-bucket/telegrambot-english-0.0.1-SNAPSHOT-all.jar . >> /home/ec2-user/awss3cplog 2>&1
java -jar telegrambot-english-0.0.1-SNAPSHOT-all.jar ${bot_credentials} ${bot_name}   >> /home/ec2-user/javarunlog 2>&1
