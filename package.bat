@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===================================================
echo   TeamUp Desktop Application Packager (jpackage)
echo ===================================================
echo.

:: 1. Find JDK tools
set "JAVA="
set "JAR="
set "JPACKAGE="

if exist "jdk\bin\java.exe" set "JAVA=jdk\bin\java.exe"
if exist "jdk\bin\jar.exe" set "JAR=jdk\bin\jar.exe"
if exist "jdk\bin\jpackage.exe" set "JPACKAGE=jdk\bin\jpackage.exe"

if not defined JAR if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR=%JAVA_HOME%\bin\jar.exe"
if not defined JPACKAGE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"

if not defined JAR for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\jar.exe" set "JAR=%%D\bin\jar.exe"
if not defined JPACKAGE for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\jpackage.exe" set "JPACKAGE=%%D\bin\jpackage.exe"

if not defined JAR set "JAR=jar"
if not defined JPACKAGE set "JPACKAGE=jpackage"

:: 2. Compile source code
echo [1/4] Compiling Java source files...
call "%~dp0build.bat"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed. Exiting...
    exit /b %ERRORLEVEL%
)

:: 3. Prepare distribution directory ^& JAR
echo.
echo [2/4] Packaging executable JAR (TeamUp.jar)...
if not exist "dist" mkdir "dist"
if not exist "dist\jar" mkdir "dist\jar"

"%JAR%" --create --file "dist\jar\TeamUp.jar" --main-class Main -C bin .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JAR packaging failed.
    exit /b %ERRORLEVEL%
)
copy /y "dist\jar\TeamUp.jar" "dist\TeamUp.jar" >nul

:: 4. Build standalone application bundle with jpackage
echo.
echo [3/4] Building standalone Windows application with jpackage...
echo       (Bundles its own lightweight JRE, zero Java install required by end users)

set "ICON_ARG="
if exist "assets\teamup.ico" set "ICON_ARG=--icon assets\teamup.ico"

if exist "dist\TeamUp" (
    echo       Cleaning previous bundle...
    rmdir /s /q "dist\TeamUp" 2>nul
)

"%JPACKAGE%" --type app-image ^
    --name "TeamUp" ^
    --input "dist\jar" ^
    --main-jar "TeamUp.jar" ^
    --main-class "Main" ^
    !ICON_ARG! ^
    --dest "dist" ^
    --app-version "1.0.0" ^
    --vendor "TeamUp" ^
    --copyright "Copyright 2026 TeamUp" ^
    --java-options "-Dawt.useSystemAAFontSettings=on"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] jpackage failed to generate app-image.
    exit /b %ERRORLEVEL%
)

:: Check for WiX Toolset to create single EXE/MSI installer
where candle.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [*] WiX Toolset detected. Generating Windows MSI/EXE installer...
    "%JPACKAGE%" --type exe ^
        --name "TeamUp-Setup" ^
        --input "dist\jar" ^
        --main-jar "TeamUp.jar" ^
        --main-class "Main" ^
        !ICON_ARG! ^
        --dest "dist" ^
        --app-version "1.0.0" ^
        --vendor "TeamUp" ^
        --win-dir-chooser ^
        --win-shortcut ^
        --win-menu ^
        --java-options "-Dawt.useSystemAAFontSettings=on"
)

:: 5. Create zip distribution
echo.
echo [4/4] Creating portable zip archive...
powershell -Command "if (Test-Path 'dist\TeamUp') { Compress-Archive -Path 'dist\TeamUp\*' -DestinationPath 'dist\TeamUp-Windows-x64.zip' -Force; Write-Host '      Created dist\TeamUp-Windows-x64.zip' }"

echo.
echo ===================================================
echo   [SUCCESS] Packaging complete!
echo ===================================================
echo   Portable App: dist\TeamUp\TeamUp.exe
echo   ZIP Archive : dist\TeamUp-Windows-x64.zip
echo   Executable  : dist\TeamUp.jar
echo ===================================================
endlocal
