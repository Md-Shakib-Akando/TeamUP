@echo off
setlocal
cd /d "%~dp0"

set "JAVAC="
if exist "jdk\bin\javac.exe" set "JAVAC=jdk\bin\javac.exe"
if not defined JAVAC if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
if not defined JAVAC for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\javac.exe" set "JAVAC=%%D\bin\javac.exe"
if not defined JAVAC set "JAVAC=javac"

if not exist "bin" (
    mkdir "bin"
) else (
    del /q "bin\*.class" 2>nul
    for /d %%p in (bin\*) do rmdir /s /q "%%p" 2>nul
)

echo Compiling TeamUp Platform (src to bin)...
"%JAVAC%" -d bin src\*.java src\sections\*.java src\widgets\*.java src\dashboard\admin\*.java src\dashboard\student\*.java tests\*.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Build succeeded! Run run.bat, ./run.sh or "java -cp bin Main".
) else (
    echo [ERROR] Compilation failed.
)
endlocal
