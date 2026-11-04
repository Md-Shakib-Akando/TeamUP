@echo off
setlocal
cd /d "%~dp0"
if exist "jdk\bin\javac.exe" (
    set "JAVAC=jdk\bin\javac.exe"
) else (
    set "JAVAC=javac"
)

echo Compiling TeamUp Landing Page (Java 8 compatible, output to .)...
"%JAVAC%" --release 8 -d . *.java sections\*.java widgets\*.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Build succeeded! Run ./run.sh, run.bat or "java Main".
) else (
    echo [ERROR] Compilation failed.
)
endlocal
