#!/bin/bash
echo "Starting application in DEVELOPMENT mode..."
java -jar -Dspring.profiles.active=dev target/backend-0.0.1-SNAPSHOT.jar 