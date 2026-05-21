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

world="${SIM_MC_WORLD:-run/saves/New World}"
start="${SIM_MC_START:-386,61,42}"
goal="${SIM_MC_GOAL:-500,61,40}"
routes="${SIM_MC_ROUTES:-crash_383_61_44_to_500_62_40@383,61,44@500,62,40;crash_384_63_43_to_500_62_40@384,63,43@500,62,40;crash_385_62_42_to_500_62_40@385,62,42@500,62,40;crash_386_61_42_to_500_61_40@386,61,42@500,61,40}"
runs="${SIM_MC_RUNS:-3}"
max_ticks="${SIM_MC_MAX_TICKS:-1200}"
radius="${SIM_MC_RADIUS_CHUNKS:-8}"
goal_search="${SIM_MC_GOAL_SEARCH_BLOCKS:-160}"
max_final_distance="${SIM_MC_MAX_FINAL_DISTANCE:-2.0}"
max_stuck_events="${SIM_MC_MAX_STUCK_EVENTS:-12}"
max_recovery_attempts="${SIM_MC_MAX_RECOVERY_ATTEMPTS:-6}"
max_backward_ticks="${SIM_MC_MAX_BACKWARD_TICKS:-80}"
max_average_lateral_error="${SIM_MC_MAX_AVERAGE_LATERAL_ERROR:-0.25}"
save_replay="${SIM_MC_SAVE_REPLAY:-true}"
max_heap="${SIM_MC_MAX_HEAP:-4g}"

exec ./gradlew :tools:pathfinder-sim:minecraftRegression \
    "-PsimMinecraftWorld=$world" \
    "-PsimMinecraftStart=$start" \
    "-PsimMinecraftGoal=$goal" \
    "-PsimMinecraftRoutes=$routes" \
    "-PsimMinecraftRuns=$runs" \
    "-PsimMinecraftMaxTicks=$max_ticks" \
    "-PsimMinecraftRadiusChunks=$radius" \
    "-PsimMinecraftGoalSearchBlocks=$goal_search" \
    "-PsimMinecraftMaxFinalDistance=$max_final_distance" \
    "-PsimMinecraftMaxStuckEvents=$max_stuck_events" \
    "-PsimMinecraftMaxRecoveryAttempts=$max_recovery_attempts" \
    "-PsimMinecraftMaxBackwardTicks=$max_backward_ticks" \
    "-PsimMinecraftMaxAverageLateralError=$max_average_lateral_error" \
    "-PsimMinecraftMaxHeap=$max_heap" \
    "-PsimMinecraftSaveReplay=$save_replay" \
    --console=plain
