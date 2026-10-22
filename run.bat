@echo off
title NexCart E-Commerce Platform Server
cd /d "%~dp0"

echo =======================================================
echo   Starting NexCart E-Commerce Platform (Port 8081)...
echo =======================================================
echo.

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
set PATH=C:\Users\Sumanth Puli\.gemini\antigravity\scratch\mvn\apache-maven-3.9.6\bin;%JAVA_HOME%\bin;%PATH%

echo Checking Java & Maven environment...
echo Project Directory: %CD%
echo.
echo Launching Application on http://localhost:8081 ...
echo Admin credentials: admin@gmail.com / Admin@123
echo.

call mvn spring-boot:run
if errorlevel 1 (
    echo.
    echo [Fallback] Starting packaged JAR directly...
    java -jar target\ecommerce-java-1.0.0.jar
)

pause
