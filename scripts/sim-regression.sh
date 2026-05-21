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

runs="${SIM_REGRESSION_RUNS:-20}"
max_ticks="${SIM_REGRESSION_MAX_TICKS:-600}"
max_final_distance="${SIM_REGRESSION_MAX_FINAL_DISTANCE:-1.25}"
max_stuck_events="${SIM_REGRESSION_MAX_STUCK_EVENTS:-5}"
save_replay="${SIM_REGRESSION_SAVE_REPLAY:-false}"

exec ./gradlew :tools:pathfinder-sim:regression \
    "-PsimRegressionRuns=$runs" \
    "-PsimRegressionMaxTicks=$max_ticks" \
    "-PsimRegressionMaxFinalDistance=$max_final_distance" \
    "-PsimRegressionMaxStuckEvents=$max_stuck_events" \
    "-PsimRegressionSaveReplay=$save_replay" \
    --console=plain
