@echo off
setlocal
cd /d "%~dp0"

if exist "jdk\bin\javac.exe" (
    set "JAVAC=jdk\bin\javac.exe"
) else (
    set "JAVAC=javac"
)

if not exist "bin" mkdir "bin"

echo Compiling TeamUp Platform (src -> bin, Java 8 compatible)...
"%JAVAC%" --release 8 -d bin src\*.java src\sections\*.java src\widgets\*.java src\dashboard\*.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Build succeeded! Run run.bat, ./run.sh or "java -cp bin Main".
) else (
    echo [ERROR] Compilation failed.
)
endlocal
