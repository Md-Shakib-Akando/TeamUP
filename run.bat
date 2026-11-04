@echo off
setlocal
cd /d "%~dp0"
if exist "jdk\bin\java.exe" (
    set "JAVA=jdk\bin\java.exe"
) else (
    set "JAVA=java"
)

echo Starting TeamUp Landing Page...
"%JAVA%" -Dawt.useSystemAAFontSettings=on -cp . Main
endlocal
