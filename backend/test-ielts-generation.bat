@echo off
echo Testing IELTS Speaking Test Generation
echo =====================================

echo.
echo This script will test the new IELTS test generation functionality.
echo Make sure the application is running on http://localhost:8080
echo.

set /p AUTH_TOKEN=Enter your JWT token (or press Enter to skip authentication): 

if "%AUTH_TOKEN%"=="" (
    echo No token provided, testing without authentication...
    curl -X POST http://localhost:8080/api/v1/ielts-test/generate-structured -H "Content-Type: application/json"
) else (
    echo Testing with authentication...
    curl -X POST http://localhost:8080/api/v1/ielts-test/generate-structured -H "Content-Type: application/json" -H "Authorization: Bearer %AUTH_TOKEN%"
)

echo.
echo.
echo Check the project directory for generated files:
echo - deepseek_request_*.json (API request)
echo - deepseek_response_*.json (API response)
echo - ielts_test_generation_*.json (Generated test)
echo.

pause
