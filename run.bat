@echo off
title University Management System (Spring Boot)
echo ======================================================================
echo   Starting University Management System Web Application on Port 8085
echo ======================================================================
echo.
echo Application URL: http://localhost:8085
echo H2 Console URL:  http://localhost:8085/h2-console
echo.
java -jar target\university-portal-1.0.0.jar
pause
