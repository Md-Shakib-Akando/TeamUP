#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

echo "==================================================="
echo "  TeamUp Desktop Application Packager (jpackage)"
echo "==================================================="
echo

# 1. Compile source code
echo "[1/4] Compiling Java source files..."
./build.sh

# 2. Find tools
JAR="jar"
JPACKAGE="jpackage"
if [ -f "./jdk/bin/jar.exe" ]; then JAR="./jdk/bin/jar.exe"; elif [ -f "./jdk/bin/jar" ]; then JAR="./jdk/bin/jar"; fi
if [ -f "./jdk/bin/jpackage.exe" ]; then JPACKAGE="./jdk/bin/jpackage.exe"; elif [ -f "./jdk/bin/jpackage" ]; then JPACKAGE="./jdk/bin/jpackage"; fi

# 3. Create JAR
echo "[2/4] Packaging executable JAR (TeamUp.jar)..."
mkdir -p dist/jar
"$JAR" --create --file dist/jar/TeamUp.jar --main-class Main -C bin .
cp dist/jar/TeamUp.jar dist/TeamUp.jar

# 4. Run jpackage
echo "[3/4] Building standalone application with jpackage..."
rm -rf dist/TeamUp

ICON_ARG=()
if [ -f "assets/teamup.ico" ]; then
    ICON_ARG=(--icon "assets/teamup.ico")
fi

"$JPACKAGE" --type app-image \
    --name "TeamUp" \
    --input dist/jar \
    --main-jar TeamUp.jar \
    --main-class Main \
    "${ICON_ARG[@]}" \
    --dest dist \
    --app-version 1.0.0 \
    --vendor "TeamUp" \
    --copyright "Copyright 2026 TeamUp" \
    --java-options "-Dawt.useSystemAAFontSettings=on"

# 5. Zip distribution
echo "[4/4] Creating archive..."
if [ -d "dist/TeamUp" ]; then
    if command -v powershell.exe >/dev/null 2>&1; then
        powershell.exe -Command "Compress-Archive -Path 'dist\TeamUp\*' -DestinationPath 'dist\TeamUp-Windows-x64.zip' -Force"
        echo "      Created dist/TeamUp-Windows-x64.zip"
    elif command -v zip >/dev/null 2>&1; then
        (cd dist && zip -r TeamUp-Windows-x64.zip TeamUp)
        echo "      Created dist/TeamUp-Windows-x64.zip"
    else
        (cd dist && tar -czf TeamUp-portable.tar.gz TeamUp)
        echo "      Created dist/TeamUp-portable.tar.gz"
    fi
fi

echo "==================================================="
echo "  [SUCCESS] Packaging complete!"
echo "  Output in: dist/TeamUp"
echo "==================================================="
