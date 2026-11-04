#!/usr/bin/env bash
cd "$(dirname "$0")"

if [ -f "./jdk/bin/java.exe" ]; then
    ./jdk/bin/java.exe -Dawt.useSystemAAFontSettings=on -cp . Main
elif [ -f "./jdk/bin/java" ]; then
    ./jdk/bin/java -Dawt.useSystemAAFontSettings=on -cp . Main
else
    java -Dawt.useSystemAAFontSettings=on -cp . Main
fi
