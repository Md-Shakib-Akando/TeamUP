@echo off
setlocal
cd /d "%~dp0"

set "JAVA="
if exist "jdk\bin\java.exe" set "JAVA=jdk\bin\java.exe"
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAVA for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\java.exe" set "JAVA=%%D\bin\java.exe"
if not defined JAVA set "JAVA=java"

echo Starting TeamUp Landing Page...
"%JAVA%" -Dawt.useSystemAAFontSettings=on -cp . Main
endlocal
