@echo off
REM Run script for Windows

echo ========================================
echo Running Java Swing Chat Application
echo ========================================
echo.

if not exist "bin\main\ChatApplication.class" (
    echo Error: Application not compiled yet!
    echo Please run compile.bat first.
    echo.
    pause
    exit /b 1
)

echo Starting application...
echo.
java -cp "lib/okhttp-4.12.0.jar;lib/okio-jvm-3.6.0.jar;lib/gson-2.10.1.jar;lib/kotlin-stdlib-1.9.0.jar;bin" main.ChatApplication

pause