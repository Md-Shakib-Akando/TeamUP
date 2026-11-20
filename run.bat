@echo off
setlocal
cd /d "%~dp0"

call build.bat
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed. Exiting...
    pause
    exit /b %ERRORLEVEL%
)

set "JAVA="
if exist "jdk\bin\java.exe" set "JAVA=jdk\bin\java.exe"
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\java.exe" set "JAVA=%%D\bin\java.exe"
if not defined JAVA set "JAVA=java"

echo Starting TeamUp Platform...

REM Backend: the live API on Render by default.
REM For local development:  set TEAMUP_API_URL=http://localhost:5050/api  before running this file.
if not defined TEAMUP_API_URL set "TEAMUP_API_URL=https://teamup-r3oz.onrender.com/api"
echo [TeamUp] Backend: %TEAMUP_API_URL%

REM Only start the local Node.js server when the API is local and nothing is on port 5050 yet
echo %TEAMUP_API_URL% | findstr /i "localhost 127.0.0.1" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    netstat -ano | findstr :5050 >nul 2>&1
    if errorlevel 1 if exist "server\server.js" (
        echo [TeamUp] Starting local Node.js backend server...
        pushd "%~dp0server"
        start "TeamUp Backend" /B node server.js
        popd
        timeout /t 2 /nobreak >nul 2>&1
    )
)

"%JAVA%" -Dawt.useSystemAAFontSettings=on -cp bin Main %*
endlocal
