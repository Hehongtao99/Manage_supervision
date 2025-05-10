@echo off
echo Starting application in PRODUCTION mode...
java -jar -Dspring.profiles.active=prod target/backend-0.0.1-SNAPSHOT.jar
pause 