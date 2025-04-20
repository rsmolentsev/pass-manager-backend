#!/bin/bash

export JWT_SECRET="your-secret-key-here"
export DB_URL="jdbc:postgresql://localhost:5432/pass_manager"
export DB_USER="pass_manager"
export DB_PASSWORD="pass_manager"

./gradlew run 