@echo off
setlocal
cd /d "%~dp0"

call build.bat
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed. Exiting...
    pause
    exit /b %ERRORLEVEL%
)

if exist "jdk\bin\java.exe" (
    set "JAVA=jdk\bin\java.exe"
) else (
    set "JAVA=java"
)

echo Starting TeamUp Platform...
"%JAVA%" -Dawt.useSystemAAFontSettings=on -cp bin Main %*
endlocal
