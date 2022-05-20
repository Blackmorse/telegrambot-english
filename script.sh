#!/bin/bash

./gradlew run --args="$(cat ./creds.txt)"
