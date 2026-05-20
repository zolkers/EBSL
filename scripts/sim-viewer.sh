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
replay_dir="${VIEWER_REPLAY_DIR:-$HOME/.ebsl/pathfinder-sim/replays}"
world_dir="${VIEWER_WORLD_DIR:-run/saves}"
open_browser=true
docker_mode=false

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
        --replay-dir)
            replay_dir="$2"
            shift 2
            ;;
        --world-dir)
            world_dir="$2"
            shift 2
            ;;
        --docker)
            docker_mode=true
            shift
            ;;
        *)
            echo "Unknown argument: $1" >&2
            echo "Usage: scripts/sim-viewer.sh [--docker] [--port 8087] [--bind-address 0.0.0.0] [--world-dir path] [--replay-dir path] [--no-open]" >&2
            exit 1
            ;;
    esac
done

url="http://localhost:$port"

if [ "$docker_mode" = true ]; then
    case "$world_dir" in
        /*) resolved_world_dir="$world_dir" ;;
        *) resolved_world_dir="$repo_dir/$world_dir" ;;
    esac
    if [ ! -d "$resolved_world_dir" ]; then
        echo "World directory does not exist: $resolved_world_dir" >&2
        exit 1
    fi
    export VIEWER_PORT="$port"
    export VIEWER_WORLD_DIR="$resolved_world_dir"

    echo "Serving pathfinder sim viewer with Docker at $url"
    echo "Mounting worlds from $resolved_world_dir to /workspace/run/saves"
    echo "Building image and starting isolated viewer server."
    echo "Press Ctrl+C to stop the viewer server."

    if [ "$open_browser" = true ]; then
        if command -v xdg-open >/dev/null 2>&1; then
            (sleep 8 && xdg-open "$url" >/dev/null 2>&1) &
        elif command -v open >/dev/null 2>&1; then
            (sleep 8 && open "$url" >/dev/null 2>&1) &
        fi
    fi

    exec docker compose up --build pathfinder-sim-viewer
fi

echo "Serving pathfinder sim viewer at $url"
echo "Bound to $bind_address for LAN/mobile testing."
echo "Serving Java replay API from $replay_dir"
echo "Building and syncing viewer assets before launch."
echo "Press Ctrl+C to stop the viewer server."

./gradlew :tools:pathfinder-sim-viewer:check :tools:pathfinder-sim-server:processResources \
    "-Pviewer.port=$port" \
    "-Pviewer.bindAddress=$bind_address" \
    "-Pviewer.replayDir=$replay_dir" \
    --console=plain

if [ "$open_browser" = true ]; then
    if command -v xdg-open >/dev/null 2>&1; then
        (sleep 2 && xdg-open "$url" >/dev/null 2>&1) &
    elif command -v open >/dev/null 2>&1; then
        (sleep 2 && open "$url" >/dev/null 2>&1) &
    fi
fi

./gradlew :tools:pathfinder-sim-server:bootRun \
    "-Pviewer.port=$port" \
    "-Pviewer.bindAddress=$bind_address" \
    "-Pviewer.replayDir=$replay_dir" \
    --console=plain
