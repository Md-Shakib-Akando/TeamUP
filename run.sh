#!/usr/bin/env bash
cd "$(dirname "$0")"

./build.sh
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed. Exiting..."
    exit 1
fi

if [ -f "./jdk/bin/java.exe" ]; then
    ./jdk/bin/java.exe -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
elif [ -f "./jdk/bin/java" ]; then
    ./jdk/bin/java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
else
    java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
fi
