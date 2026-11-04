#!/usr/bin/env bash
cd "$(dirname "$0")"

if [ ! -f "bin/Main.class" ]; then
    echo "[INFO] Binaries not found, compiling first..."
    ./build.sh
fi

if [ -f "./jdk/bin/java.exe" ]; then
    ./jdk/bin/java.exe -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
elif [ -f "./jdk/bin/java" ]; then
    ./jdk/bin/java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
else
    java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
fi
