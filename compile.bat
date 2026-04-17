@echo off
REM Compile script for Windows

echo ========================================
echo Compiling Java Swing Chat Application
echo ========================================

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all Java files
echo Compiling source files...
javac -cp "lib\okhttp-4.12.0.jar;lib\okio-jvm-3.6.0.jar;lib\gson-2.10.1.jar;src" -d bin src\model\*.java src\service\*.java src\controller\*.java src\ui\*.java src\main\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation successful!
    echo ========================================
    echo.
    echo To run the application, execute:
    echo   run.bat
    echo.
) else (
    echo.
    echo ========================================
    echo Compilation failed!
    echo ========================================
    echo Please check for errors above.
)

pause