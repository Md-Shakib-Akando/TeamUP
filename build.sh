#!/usr/bin/env bash
cd "$(dirname "$0")"

mkdir -p bin

if [ -f "./jdk/bin/javac.exe" ]; then
    ./jdk/bin/javac.exe --release 8 -d bin src/*.java src/sections/*.java src/widgets/*.java src/dashboard/*.java
elif [ -f "./jdk/bin/javac" ]; then
    ./jdk/bin/javac --release 8 -d bin src/*.java src/sections/*.java src/widgets/*.java src/dashboard/*.java
else
    javac --release 8 -d bin src/*.java src/sections/*.java src/widgets/*.java src/dashboard/*.java
fi

if [ $? -eq 0 ]; then
    echo "[OK] Build complete. Run with ./run.sh or java -cp bin Main"
fi
