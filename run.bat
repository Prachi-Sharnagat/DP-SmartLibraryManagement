@echo off
echo ===================================================
echo   Compiling Smart Library Management System...
echo ===================================================

if not exist bin mkdir bin
javac -d bin -cp "lib/*" src/model/*.java src/factory/*.java src/chain/*.java src/bridge/*.java src/observer/*.java src/database/*.java src/proxy/*.java src/service/*.java src/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo ===================================================
echo   Starting Smart Library Management System...
echo ===================================================
java -cp "bin;lib/*" Main
pause
