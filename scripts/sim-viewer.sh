#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_dir="$(CDPATH= cd -- "$script_dir/.." && pwd)"

cd "$repo_dir"

if [ -z "${JAVA_HOME:-}" ] && [ -x "$repo_dir/.gradle/ebsl-jdks/microsoft-jdk-21-x64/bin/java" ]; then
    JAVA_HOME="$repo_dir/.gradle/ebsl-jdks/microsoft-jdk-21-x64"
    export JAVA_HOME
fi

if [ -n "${JAVA_HOME:-}" ]; then
    PATH="$JAVA_HOME/bin:$PATH"
    export PATH
fi

port="${VIEWER_PORT:-8087}"
bind_address="${VIEWER_BIND_ADDRESS:-0.0.0.0}"
open_browser=true

while [ "$#" -gt 0 ]; do
    case "$1" in
        --port|-p)
            port="$2"
            shift 2
            ;;
        --bind-address|-b)
            bind_address="$2"
            shift 2
            ;;
        --no-open)
            open_browser=false
            shift
            ;;
        *)
            echo "Unknown argument: $1" >&2
            echo "Usage: scripts/sim-viewer.sh [--port 8087] [--bind-address 0.0.0.0] [--no-open]" >&2
            exit 1
            ;;
    esac
done

./gradlew :tools:pathfinder-sim-viewer:syncViewerApp

viewer_dir="$repo_dir/build/tools/pathfinder-sim-viewer/webapp"
url="http://localhost:$port"

echo "Serving pathfinder sim viewer at $url"
echo "Bound to $bind_address for LAN/mobile testing."
echo "Press Ctrl+C to stop the viewer server."

if [ "$open_browser" = true ]; then
    if command -v xdg-open >/dev/null 2>&1; then
        (sleep 2 && xdg-open "$url" >/dev/null 2>&1) &
    elif command -v open >/dev/null 2>&1; then
        (sleep 2 && open "$url" >/dev/null 2>&1) &
    fi
fi

java -m jdk.httpserver -b "$bind_address" -p "$port" -d "$viewer_dir"
