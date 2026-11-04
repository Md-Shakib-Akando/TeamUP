#!/usr/bin/env bash
cd "$(dirname "$0")"

if [ -f "./jdk/bin/javac.exe" ]; then
    ./jdk/bin/javac.exe --release 8 -d . *.java sections/*.java widgets/*.java
elif [ -f "./jdk/bin/javac" ]; then
    ./jdk/bin/javac --release 8 -d . *.java sections/*.java widgets/*.java
else
    javac --release 8 -d . *.java sections/*.java widgets/*.java
fi
echo "[OK] Build complete. Run with ./run.sh or java Main"
