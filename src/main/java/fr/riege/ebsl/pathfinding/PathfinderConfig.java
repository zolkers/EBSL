package fr.riege.ebsl.pathfinding;

public class PathfinderConfig {

    public static final Value<Integer> PATHFINDER_MAX_JUMP_HEIGHT = new Value<>(1);
    public static final Value<Boolean> SHOW_DEBUG = new Value<>(true);
    public static final Value<Double> WALK_COST = new Value<>(0.0);
    public static final Value<Double> DIAGONAL_COST = new Value<>(0.0);
    public static final Value<Double> FULL_STEP_ASCENT_BASE_COST = new Value<>(2.0);
    public static final Value<Double> FULL_STEP_ASCENT_DY_COST = new Value<>(0.5);
    public static final Value<Double> PARTIAL_ASCENT_COST = new Value<>(0.0);
    public static final Value<Double> JUMP_COST = new Value<>(0.0);
    public static final Value<Double> PARKOUR_COST = new Value<>(0.0);
    public static final Value<Double> FALL_DY_COST = new Value<>(0.1);
    public static final Value<Double> SWIM_COST = new Value<>(0.0);
    public static final Value<Double> CLIMB_COST = new Value<>(0.0);
    public static final Value<Double> CARDINAL_WALL_COST = new Value<>(0.55);
    public static final Value<Double> DIAGONAL_WALL_COST = new Value<>(0.25);
    public static final Value<Double> PARTIAL_ASCENT_EDGE_COST = new Value<>(0.32);
    public static final Value<Double> PARTIAL_ASCENT_ENTRY_SIDE_COST = new Value<>(0.28);
    public static final Value<Double> OPENING_ENTRY_IMBALANCE_COST = new Value<>(0.24);
    public static final Value<Boolean> CORNER_STEERING_ENABLED = new Value<>(true);
    public static final Value<Boolean> CORNER_STEERING_SLOWDOWN = new Value<>(true);
    public static final Value<Double> CORNER_STEERING_SCAN_RADIUS = new Value<>(0.42);
    public static final Value<Double> CORNER_STEERING_NUDGE_WEIGHT = new Value<>(0.55);
    public static final Value<Double> CORNER_STEERING_CENTERLINE_WEIGHT = new Value<>(0.45);
    public static final Value<Double> CORNER_STEERING_CENTERLINE_START = new Value<>(0.18);
    public static final Value<Double> CORNER_STEERING_CENTERLINE_MAX = new Value<>(0.58);
    public static final Value<Integer> DEFAULT_WALK_MAX_ITERATIONS = new Value<>(100000);
    public static final Value<Integer> DEFAULT_WALK_MAX_LENGTH = new Value<>(12500);
    public static final Value<Integer> INSTANT_WALK_MAX_ITERATIONS = new Value<>(12000);
    public static final Value<Integer> INSTANT_WALK_MAX_LENGTH = new Value<>(1800);
    public static final Value<Integer> REPAIR_WALK_MAX_ITERATIONS = new Value<>(8000);
    public static final Value<Integer> REPAIR_WALK_MAX_LENGTH = new Value<>(600);
    public static final Value<Integer> QUEUED_LONG_RANGE_MAX_ITERATIONS = new Value<>(24000);
    public static final Value<Integer> QUEUED_LONG_RANGE_MAX_LENGTH = new Value<>(2600);
    public static final Value<Double> STUCK_DIST_THRESHOLD = new Value<>(0.2);
    public static final Value<Integer> STUCK_TIME_MS = new Value<>(400);
    public static final Value<Double> DRIFT_DISTANCE = new Value<>(4.5);
    public static final Value<Integer> REPLAN_COOLDOWN_MS = new Value<>(2500);
    public static final Value<Double> JUMP_TRIGGER_DIST = new Value<>(0.6);
    public static final Value<Double> STEP_UP_TRIGGER_DIST = new Value<>(1.0);
    public static final Value<Integer> JUMP_COOLDOWN_TICKS = new Value<>(8);
    public static final Value<Integer> STALL_JUMP_PROGRESS_MS = new Value<>(450);
    public static final Value<Double> PATH_PROGRESS_EPSILON = new Value<>(0.08);
    public static final Value<Double> WALK_TARGET_DEADZONE = new Value<>(0.28);
    public static final Value<Double> WALK_FORWARD_DOT = new Value<>(0.18);
    public static final Value<Double> WALK_BACKWARD_DOT = new Value<>(-0.45);
    public static final Value<Double> WALK_STRAFE_DOT = new Value<>(0.32);
    public static final Value<Integer> COAST_TIMEOUT_MS = new Value<>(3000);
    public static final Value<Integer> SMART_CUTOFF_COOLDOWN_MS = new Value<>(800);
    public static final Value<Integer> LOCAL_REPAIR_LOOKAHEAD = new Value<>(4);
    public static final Value<Integer> LOCAL_REPAIR_DRIFT_LOOKAHEAD = new Value<>(7);
    public static final Value<Double> LOCAL_REPAIR_DRIFT_THRESHOLD = new Value<>(1.5);
    public static final Value<Double> GOAL_REACHED_HDIST = new Value<>(1.2);
    public static final Value<Double> GOAL_REACHED_VDIST = new Value<>(2.0);
    public static final Value<Integer> UNSTUCK_JUMP_MS = new Value<>(400);
    public static final Value<Integer> UNSTUCK_BACKUP_MS = new Value<>(900);
    public static final Value<Integer> BACKUP_TICKS = new Value<>(6);
    public static final Value<Integer> PATH_REPLAN_STALE_MS = new Value<>(500);
    public static final Value<Double> PATH_REPLAN_DRIFT_DISTANCE = new Value<>(1.25);
    public static final Value<Integer> GROUNDED_NO_PROGRESS_REPLAN_MS = new Value<>(400);
    public static final Value<Integer> PATH_REPLAN_HARD_STALE_MS = new Value<>(1800);
    public static final Value<Double> BACKUP_MAX_HORIZONTAL_SPEED = new Value<>(0.22);
    public static final Value<Double> CORNER_ALIGN_MIN_DISTANCE = new Value<>(0.32);
    public static final Value<Double> CORNER_ALIGN_MAX_DISTANCE = new Value<>(1.45);
    public static final Value<Double> CORNER_ALIGN_MAX_VERTICAL = new Value<>(0.80);
    public static final Value<Integer> CORNER_ALIGN_MAX_MS = new Value<>(900);
    public static final Value<Double> SEGMENT_RECALC_RATIO = new Value<>(0.70);
    public static final Value<Double> EARLY_SEGMENT_RECALC_RATIO = new Value<>(0.50);
    public static final Value<Double> HORIZON_TRIM_RATIO = new Value<>(0.75);
    public static final Value<Double> PREPARE_REMAINING_DISTANCE = new Value<>(45.0);
    public static final Value<Double> EMERGENCY_REMAINING_DISTANCE = new Value<>(14.0);
    public static final Value<Double> PREPARED_SWITCH_REMAINING_DISTANCE = new Value<>(24.0);
    public static final Value<Double> FINAL_GOAL_XZ_TOLERANCE = new Value<>(1.75);
    public static final Value<Double> MAX_SEGMENT_DISTANCE = new Value<>(150.0);
    public static final Value<Integer> SEGMENT_RETRY_COOLDOWN_MS = new Value<>(1500);
    public static final Value<Integer> PLAYER_START_AFTER_FAILURES = new Value<>(2);
    public static final Value<Double> PLAYER_START_RECOVERY_RATIO = new Value<>(0.90);
    public static final Value<Boolean> USE_CAMERA_RAIL = new Value<>(true);
    public static final Value<Double> CAMERA_RAIL_REACHED_DIST = new Value<>(1.15);
    public static final Value<Double> LEGACY_CAMERA_EYE_Y = new Value<>(1.6);
    public static final Value<Double> CAMERA_RAIL_GUIDE_LOOKAHEAD_DIST = new Value<>(3.5);
    public static final Value<Integer> ROTATION_REDISPATCH_COOLDOWN_MS = new Value<>(220);
    public static final Value<Double> IDLE_YAW_DEADBAND_DEG = new Value<>(2.0);
    public static final Value<Double> IDLE_PITCH_DEADBAND_DEG = new Value<>(3.0);
    public static final Value<Double> ACTIVE_YAW_RETARGET_DEG = new Value<>(6.0);
    public static final Value<Double> ACTIVE_PITCH_RETARGET_DEG = new Value<>(5.0);
    public static final Value<Integer> ROTATION_DURATION_MS = new Value<>(550);
    public static final Value<Double> PITCH_MIN_HORIZONTAL_DISTANCE = new Value<>(2.25);
    public static final Value<Double> PITCH_LAND_DEADBAND_DEG = new Value<>(3.0);
    public static final Value<Double> PITCH_WATER_DEADBAND_DEG = new Value<>(10.0);
    public static final Value<Double> PITCH_LAND_MAX_ABS_DEG = new Value<>(22.0);
    public static final Value<Double> PITCH_WATER_MAX_ABS_DEG = new Value<>(8.0);
    public static final Value<Double> PITCH_LAND_MAX_STEP_DEG = new Value<>(7.0);
    public static final Value<Double> PITCH_WATER_MAX_STEP_DEG = new Value<>(3.0);
    public static final Value<Double> PITCH_SNAP_TO_NEUTRAL_DEG = new Value<>(1.0);
    public static final Value<Integer> CAMERA_LOOKAHEAD = new Value<>(32);
    public static final Value<Double> CAMERA_MAX_LATERAL_DEV = new Value<>(2.5);
    public static final Value<Integer> SMOOTH_OPEN_SKIP_BUDGET = new Value<>(12);
    public static final Value<Integer> SMOOTH_MID_SKIP_BUDGET = new Value<>(7);
    public static final Value<Integer> SMOOTH_TIGHT_SKIP_BUDGET = new Value<>(4);
    public static final Value<Integer> SMOOTH_OPEN_WALL_SCORE_MAX = new Value<>(2);
    public static final Value<Integer> SMOOTH_MID_WALL_SCORE_MAX = new Value<>(6);
    public static final Value<Double> SMOOTH_CONSTRAINED_CORNER_ANGLE_DEG = new Value<>(35.0);
    public static final Value<Double> INTERMEDIATE_SPACING = new Value<>(4.0);
    public static final Value<Double> PARTIAL_ASCENT_THRESHOLD = new Value<>(0.2);
    public static final Value<Double> DESCENT_THRESHOLD = new Value<>(-0.1);
    public static final Value<Double> HUGE_DEVIATION_HORIZONTAL_DISTANCE = new Value<>(12.0);
    public static final Value<Double> SMART_CUTOFF_MAX_HORIZONTAL_DISTANCE = new Value<>(1.35);
    public static final Value<Double> SMART_CUTOFF_MAX_VERTICAL_DISTANCE = new Value<>(1.75);
    public static final Value<Double> SMART_CUTOFF_MIN_PROGRESS_SKIP = new Value<>(1.25);
    public static final Value<Integer> SUSTAINED_OFF_PATH_MAX_MS = new Value<>(5000);
    public static final Value<Integer> SUSTAINED_VERTICAL_OFF_PATH_MAX_MS = new Value<>(7000);
    public static final Value<Double> OFF_PATH_HORIZONTAL_DISTANCE = new Value<>(3.0);
    public static final Value<Double> OFF_PATH_VERTICAL_DISTANCE = new Value<>(3.0);
    public static final Value<Integer> ANOMALOUS_MIN_SEGMENT_SKIP = new Value<>(3);
    public static final Value<Double> ANOMALOUS_MAX_HORIZONTAL_DISTANCE = new Value<>(5.5);
    public static final Value<Double> ANOMALOUS_MAX_VERTICAL_DISTANCE = new Value<>(4.5);
    public static final Value<Double> ANOMALOUS_MIN_NEAREST_ADVANTAGE = new Value<>(1.25);

    public static class Value<T> {
        private T value;

        public Value(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
