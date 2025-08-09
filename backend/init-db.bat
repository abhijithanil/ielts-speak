@echo off
echo PostgreSQL Database Setup for IELTS Speaking App
echo ================================================

echo.
echo This script will help you set up the PostgreSQL database.
echo Make sure PostgreSQL is installed and running on port 5432.
echo.

set /p DB_NAME=Enter database name (default: ielts_speaking_app): 
if "%DB_NAME%"=="" set DB_NAME=ielts_speaking_app

set /p DB_USER=Enter database user (default: ytms_user):
if "%DB_USER%"=="" set DB_USER=ytms_user

set /p DB_PASSWORD=Enter database password (default: ytms_password):
if "%DB_PASSWORD%"=="" set DB_PASSWORD=ytms_password

echo.
echo Setting up PostgreSQL for user '%DB_USER%' and database '%DB_NAME%'...
echo.

REM First, connect as postgres superuser to create the user and database
echo Creating user '%DB_USER%'...
psql -U postgres -c "CREATE USER %DB_USER% WITH PASSWORD '%DB_PASSWORD%';" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo User '%DB_USER%' already exists or could not be created.
)

echo Creating database '%DB_NAME%'...
psql -U postgres -c "CREATE DATABASE %DB_NAME% OWNER %DB_USER%;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Database '%DB_NAME%' already exists or could not be created.
)

echo Granting privileges to user '%DB_USER%' on database '%DB_NAME%'...
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE %DB_NAME% TO %DB_USER%;" 2>nul

echo.
echo PostgreSQL setup completed!
echo.
echo Database Configuration:
echo - Database: %DB_NAME%
echo - User: %DB_USER%
echo - Password: %DB_PASSWORD%
echo.
echo Next steps:
echo 1. Start the application: mvn spring-boot:run
echo 2. The application will automatically create the required tables
echo 3. Access the application at: http://localhost:8080/api
echo.

pause
