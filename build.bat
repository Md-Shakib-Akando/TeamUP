@echo off
setlocal
cd /d "%~dp0"

set "JAVAC="
if exist "jdk\bin\javac.exe" set "JAVAC=jdk\bin\javac.exe"
if not defined JAVAC if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if not defined JAVAC for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\javac.exe" set "JAVAC=%%D\bin\javac.exe"
if not defined JAVAC set "JAVAC=javac"

echo Compiling TeamUp Landing Page...
"%JAVAC%" -d . *.java sections\*.java widgets\*.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Build succeeded! Run .\run.bat or "java Main".
) else (
    echo [ERROR] Compilation failed.
)
endlocal
