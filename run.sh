#!/usr/bin/env bash
cd "$(dirname "$0")"

./build.sh
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed. Exiting..."
    exit 1
fi

# Auto-start Node.js + MongoDB Atlas backend if not already running on port 5000
if ! curl -s http://localhost:5000/api/health >/dev/null 2>&1; then
    if [ -f "server/server.js" ]; then
        echo "[TeamUp] Starting Node.js backend server (MongoDB Atlas)..."
        (cd server && node server.js >/dev/null 2>&1 &)
        sleep 2
    fi
fi

if [ -f "./jdk/bin/java.exe" ]; then
    ./jdk/bin/java.exe -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
elif [ -f "./jdk/bin/java" ]; then
    ./jdk/bin/java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
else
    java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
fi
