#!/usr/bin/env bash
cd "$(dirname "$0")"

./build.sh
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed. Exiting..."
    exit 1
fi

# Backend: the live API on Render by default.
# For local development:  TEAMUP_API_URL=http://localhost:5050/api ./run.sh
export TEAMUP_API_URL="${TEAMUP_API_URL:-https://teamup-r3oz.onrender.com/api}"
echo "[TeamUp] Backend: $TEAMUP_API_URL"

# Only start the local Node.js server when the API is local and it isn't running yet
case "$TEAMUP_API_URL" in
    *localhost*|*127.0.0.1*)
        if ! curl -s http://localhost:5050/api/health >/dev/null 2>&1 && [ -f "server/server.js" ]; then
            echo "[TeamUp] Starting local Node.js backend server..."
            (cd server && node server.js >/dev/null 2>&1 &)
            sleep 2
        fi
        ;;
esac

if [ -f "./jdk/bin/java.exe" ]; then
    ./jdk/bin/java.exe -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
elif [ -f "./jdk/bin/java" ]; then
    ./jdk/bin/java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
else
    java -Dawt.useSystemAAFontSettings=on -cp bin Main "$@"
fi
