@echo off
echo 🚀 Starting IELTS Speaking Practice Application
echo ================================================

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed. Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is not installed. Please install Node.js 16 or higher.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven is not installed. Please install Maven 3.6 or higher.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed

REM Start backend
echo 🔧 Starting backend...
cd backend
start "Backend" mvn spring-boot:run
cd ..

REM Wait for backend to start
echo ⏳ Waiting for backend to start...
timeout /t 10 /nobreak >nul

REM Start frontend
echo 🎨 Starting frontend...
cd frontend
if not exist node_modules (
    echo Installing frontend dependencies...
    npm install
)
start "Frontend" npm run dev
cd ..

echo ✅ Application started successfully!
echo.
echo 🌐 Frontend: http://localhost:3000
echo 🔧 Backend: http://localhost:8080
echo 📊 H2 Console: http://localhost:8080/api/h2-console
echo.
echo Press any key to exit...
pause >nul
