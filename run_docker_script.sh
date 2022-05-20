#!/bin/bash

docker-compose build && VOLUME_PATH=<path to cassandra docker volume> docker-compose up