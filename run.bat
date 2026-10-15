@echo off
echo ===================================================
echo   Compiling Smart Library Management System...
echo ===================================================

if not exist bin mkdir bin
dir /s /b java-src\*.java > sources.txt
javac -d bin -cp "lib/*" @sources.txt
set COMPILE_ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt

if %COMPILE_ERR% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %COMPILE_ERR%
)

echo ===================================================
echo   Starting Smart Library Management System...
echo ===================================================
java -cp "bin;lib/*" Main %*
pause

