package fr.riege.ebsl.pathfinding.settings;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.DoubleSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.settings.Settingable;

import java.util.List;

public final class PathfinderSettings extends Settingable {
    private static final PathfinderSettings INSTANCE = new PathfinderSettings();
    public final BooleanSetting showDebug = registerSetting(new BooleanSetting("show_debug", "Show debug", true));
    public final IntSetting maxJumpHeight = registerSetting(new IntSetting("max_jump_height", "Max jump height", 1, 1, 20));
    public final DoubleSetting walkCost = registerSetting(new DoubleSetting("walk_cost", "Walk", 0.0, 0.0, 10.0));
    public final DoubleSetting diagonalCost = registerSetting(new DoubleSetting("diagonal_cost", "Diagonal", 0.0, 0.0, 10.0));
    public final DoubleSetting fullStepAscentBaseCost = registerSetting(new DoubleSetting(
        "full_step_ascent_base_cost", "Full step ascent base", 2.0, 0.0, 20.0));
    public final DoubleSetting fullStepAscentDyCost = registerSetting(new DoubleSetting(
        "full_step_ascent_dy_cost", "Full step ascent height", 0.5, 0.0, 10.0));
    public final DoubleSetting partialAscentCost = registerSetting(new DoubleSetting(
        "partial_ascent_cost", "Slab/stair ascent", 0.0, 0.0, 10.0));
    public final DoubleSetting jumpCost = registerSetting(new DoubleSetting("jump_cost", "Jump", 0.0, 0.0, 20.0));
    public final DoubleSetting parkourCost = registerSetting(new DoubleSetting("parkour_cost", "Parkour", 0.0, 0.0, 30.0));
    public final DoubleSetting fallDyCost = registerSetting(new DoubleSetting("fall_dy_cost", "Fall height", 0.1, 0.0, 10.0));
    public final DoubleSetting swimCost = registerSetting(new DoubleSetting("swim_cost", "Swim", 0.0, 0.0, 20.0));
    public final DoubleSetting climbCost = registerSetting(new DoubleSetting("climb_cost", "Climb", 0.0, 0.0, 20.0));
    public final DoubleSetting cardinalWallCost = registerSetting(new DoubleSetting(
        "cardinal_wall_cost", "Cardinal wall proximity", 0.55, 0.0, 5.0));
    public final DoubleSetting diagonalWallCost = registerSetting(new DoubleSetting(
        "diagonal_wall_cost", "Diagonal wall proximity", 0.25, 0.0, 5.0));
    public final DoubleSetting partialAscentEdgeCost = registerSetting(new DoubleSetting(
        "partial_ascent_edge_cost", "Slab/stair edge proximity", 0.32, 0.0, 5.0));
    public final DoubleSetting partialAscentEntrySideCost = registerSetting(new DoubleSetting(
        "partial_ascent_entry_side_cost", "Slab/stair entry side", 0.28, 0.0, 5.0));
    public final DoubleSetting openingEntryImbalanceCost = registerSetting(new DoubleSetting(
        "opening_entry_imbalance_cost", "Opening side imbalance", 0.24, 0.0, 5.0));
    public final BooleanSetting cornerSteeringEnabled = registerSetting(new BooleanSetting(
        "corner_steering_enabled", "Corner steering", true));
    public final BooleanSetting cornerSteeringSlowdown = registerSetting(new BooleanSetting(
        "corner_steering_slowdown", "Slow near corners", true));
    public final DoubleSetting cornerSteeringScanRadius = registerSetting(new DoubleSetting(
        "corner_steering_scan_radius", "Corner scan radius", 0.42, 0.10, 1.00));
    public final DoubleSetting cornerSteeringNudgeWeight = registerSetting(new DoubleSetting(
        "corner_steering_nudge_weight", "Corner nudge", 0.55, 0.0, 2.0));
    public final DoubleSetting cornerSteeringCenterlineWeight = registerSetting(new DoubleSetting(
        "corner_steering_centerline_weight", "Centerline nudge", 0.45, 0.0, 2.0));
    public final DoubleSetting cornerSteeringCenterlineStart = registerSetting(new DoubleSetting(
        "corner_steering_centerline_start", "Centerline start", 0.18, 0.0, 1.0));
    public final DoubleSetting cornerSteeringCenterlineMax = registerSetting(new DoubleSetting(
        "corner_steering_centerline_max", "Centerline max", 0.58, 0.05, 2.0));
    public final IntSetting defaultWalkMaxIterations = registerSetting(new IntSetting(
        "default_walk_max_iterations", "Default max iterations", 100000, 1000, 300000));
    public final IntSetting defaultWalkMaxLength = registerSetting(new IntSetting(
        "default_walk_max_length", "Default max length", 12500, 100, 50000));
    public final IntSetting instantWalkMaxIterations = registerSetting(new IntSetting(
        "instant_walk_max_iterations", "Instant max iterations", 12000, 1000, 100000));
    public final IntSetting instantWalkMaxLength = registerSetting(new IntSetting(
        "instant_walk_max_length", "Instant max length", 1800, 100, 20000));
    public final IntSetting repairWalkMaxIterations = registerSetting(new IntSetting(
        "repair_walk_max_iterations", "Repair max iterations", 8000, 1000, 100000));
    public final IntSetting repairWalkMaxLength = registerSetting(new IntSetting(
        "repair_walk_max_length", "Repair max length", 600, 50, 10000));
    public final IntSetting queuedLongRangeMaxIterations = registerSetting(new IntSetting(
        "queued_long_range_max_iterations", "Queued segment iterations", 24000, 1000, 120000));
    public final IntSetting queuedLongRangeMaxLength = registerSetting(new IntSetting(
        "queued_long_range_max_length", "Queued segment length", 2600, 100, 30000));
    public final DoubleSetting stuckDistThreshold = registerSetting(new DoubleSetting(
        "stuck_dist_threshold", "Stuck distance", 0.2, 0.01, 1.0));
    public final IntSetting stuckTimeMs = registerSetting(new IntSetting(
        "stuck_time_ms", "Stuck time ms", 400, 100, 3000));
    public final DoubleSetting driftDistance = registerSetting(new DoubleSetting(
        "drift_distance", "Drift distance", 4.5, 0.5, 12.0));
    public final IntSetting replanCooldownMs = registerSetting(new IntSetting(
        "replan_cooldown_ms", "Replan cooldown ms", 2500, 0, 10000));
    public final DoubleSetting jumpTriggerDist = registerSetting(new DoubleSetting(
        "jump_trigger_dist", "Jump trigger distance", 0.6, 0.1, 3.0));
    public final DoubleSetting stepUpTriggerDist = registerSetting(new DoubleSetting(
        "step_up_trigger_dist", "Step-up trigger distance", 1.0, 0.1, 4.0));
    public final IntSetting jumpCooldownTicks = registerSetting(new IntSetting(
        "jump_cooldown_ticks", "Jump cooldown ticks", 8, 0, 40));
    public final IntSetting stallJumpProgressMs = registerSetting(new IntSetting(
        "stall_jump_progress_ms", "Stall jump ms", 450, 100, 3000));
    public final DoubleSetting pathProgressEpsilon = registerSetting(new DoubleSetting(
        "path_progress_epsilon", "Path progress epsilon", 0.08, 0.0, 1.0));
    public final DoubleSetting walkTargetDeadzone = registerSetting(new DoubleSetting(
        "walk_target_deadzone", "Walk target deadzone", 0.28, 0.0, 1.5));
    public final DoubleSetting walkForwardDot = registerSetting(new DoubleSetting(
        "walk_forward_dot", "Walk forward dot", 0.18, -1.0, 1.0));
    public final DoubleSetting walkBackwardDot = registerSetting(new DoubleSetting(
        "walk_backward_dot", "Walk backward dot", -0.45, -1.0, 1.0));
    public final DoubleSetting walkStrafeDot = registerSetting(new DoubleSetting(
        "walk_strafe_dot", "Walk strafe dot", 0.32, 0.0, 1.0));
    public final IntSetting coastTimeoutMs = registerSetting(new IntSetting(
        "coast_timeout_ms", "Goal coast timeout ms", 3000, 0, 15000));
    public final IntSetting smartCutoffCooldownMs = registerSetting(new IntSetting(
        "smart_cutoff_cooldown_ms", "Smart cutoff cooldown ms", 800, 0, 5000));
    public final IntSetting localRepairLookahead = registerSetting(new IntSetting(
        "local_repair_lookahead", "Repair lookahead", 4, 1, 20));
    public final IntSetting localRepairDriftLookahead = registerSetting(new IntSetting(
        "local_repair_drift_lookahead", "Repair drift lookahead", 7, 1, 30));
    public final DoubleSetting localRepairDriftThreshold = registerSetting(new DoubleSetting(
        "local_repair_drift_threshold", "Repair drift threshold", 1.5, 0.0, 8.0));
    public final DoubleSetting goalReachedHDist = registerSetting(new DoubleSetting(
        "goal_reached_hdist", "Goal horizontal tolerance", 1.2, 0.1, 5.0));
    public final DoubleSetting goalReachedVDist = registerSetting(new DoubleSetting(
        "goal_reached_vdist", "Goal vertical tolerance", 2.0, 0.1, 8.0));
    public final IntSetting unstuckJumpMs = registerSetting(new IntSetting(
        "unstuck_jump_ms", "Unstuck jump ms", 400, 100, 3000));
    public final IntSetting unstuckBackupMs = registerSetting(new IntSetting(
        "unstuck_backup_ms", "Unstuck backup ms", 900, 100, 5000));
    public final IntSetting backupTicks = registerSetting(new IntSetting(
        "backup_ticks", "Backup ticks", 6, 1, 40));
    public final IntSetting pathReplanStaleMs = registerSetting(new IntSetting(
        "path_replan_stale_ms", "Path stale repair ms", 500, 100, 5000));
    public final DoubleSetting pathReplanDriftDistance = registerSetting(new DoubleSetting(
        "path_replan_drift_distance", "Path stale drift distance", 1.25, 0.0, 8.0));
    public final IntSetting groundedNoProgressReplanMs = registerSetting(new IntSetting(
        "grounded_no_progress_replan_ms", "Grounded no progress ms", 400, 100, 5000));
    public final IntSetting pathReplanHardStaleMs = registerSetting(new IntSetting(
        "path_replan_hard_stale_ms", "Hard stale replan ms", 1800, 100, 15000));
    public final DoubleSetting backupMaxHorizontalSpeed = registerSetting(new DoubleSetting(
        "backup_max_horizontal_speed", "Backup max speed", 0.22, 0.0, 1.0));
    public final DoubleSetting cornerAlignMinDistance = registerSetting(new DoubleSetting(
        "corner_align_min_distance", "Corner align min distance", 0.32, 0.0, 2.0));
    public final DoubleSetting cornerAlignMaxDistance = registerSetting(new DoubleSetting(
        "corner_align_max_distance", "Corner align max distance", 1.45, 0.1, 5.0));
    public final DoubleSetting cornerAlignMaxVertical = registerSetting(new DoubleSetting(
        "corner_align_max_vertical", "Corner align max vertical", 0.80, 0.0, 4.0));
    public final IntSetting cornerAlignMaxMs = registerSetting(new IntSetting(
        "corner_align_max_ms", "Corner align max ms", 900, 100, 5000));
    public final DoubleSetting segmentRecalcRatio = registerSetting(new DoubleSetting(
        "segment_recalc_ratio", "Segment switch ratio", 0.70, 0.05, 1.0));
    public final DoubleSetting earlySegmentRecalcRatio = registerSetting(new DoubleSetting(
        "early_segment_recalc_ratio", "Early segment queue ratio", 0.50, 0.0, 1.0));
    public final DoubleSetting horizonTrimRatio = registerSetting(new DoubleSetting(
        "horizon_trim_ratio", "Horizon trim ratio", 0.75, 0.05, 1.0));
    public final DoubleSetting prepareRemainingDistance = registerSetting(new DoubleSetting(
        "prepare_remaining_distance", "Prepare remaining distance", 45.0, 1.0, 200.0));
    public final DoubleSetting emergencyRemainingDistance = registerSetting(new DoubleSetting(
        "emergency_remaining_distance", "Emergency remaining distance", 14.0, 1.0, 100.0));
    public final DoubleSetting preparedSwitchRemainingDistance = registerSetting(new DoubleSetting(
        "prepared_switch_remaining_distance", "Prepared switch distance", 24.0, 1.0, 150.0));
    public final DoubleSetting finalGoalXzTolerance = registerSetting(new DoubleSetting(
        "final_goal_xz_tolerance", "Final XZ tolerance", 1.75, 0.1, 8.0));
    public final DoubleSetting maxSegmentDistance = registerSetting(new DoubleSetting(
        "max_segment_distance", "Max segment distance", 150.0, 16.0, 512.0));
    public final IntSetting segmentRetryCooldownMs = registerSetting(new IntSetting(
        "segment_retry_cooldown_ms", "Segment retry cooldown ms", 1500, 0, 10000));
    public final IntSetting playerStartAfterFailures = registerSetting(new IntSetting(
        "player_start_after_failures", "Player start after failures", 2, 0, 10));
    public final DoubleSetting playerStartRecoveryRatio = registerSetting(new DoubleSetting(
        "player_start_recovery_ratio", "Player start recovery ratio", 0.90, 0.0, 1.0));
    public final BooleanSetting useCameraRail = registerSetting(new BooleanSetting(
        "use_camera_rail", "Use camera rail", true));
    public final DoubleSetting cameraRailReachedDist = registerSetting(new DoubleSetting(
        "camera_rail_reached_dist", "Camera rail reached distance", 1.15, 0.1, 5.0));
    public final DoubleSetting legacyCameraEyeY = registerSetting(new DoubleSetting(
        "legacy_camera_eye_y", "Legacy camera eye Y", 1.6, 0.0, 3.0));
    public final DoubleSetting cameraRailGuideLookaheadDist = registerSetting(new DoubleSetting(
        "camera_rail_guide_lookahead_dist", "Camera rail lookahead", 3.5, 0.1, 16.0));
    public final IntSetting rotationRedispatchCooldownMs = registerSetting(new IntSetting(
        "rotation_redispatch_cooldown_ms", "Rotation redispatch ms", 220, 0, 2000));
    public final DoubleSetting idleYawDeadbandDeg = registerSetting(new DoubleSetting(
        "idle_yaw_deadband_deg", "Idle yaw deadband", 2.0, 0.0, 30.0));
    public final DoubleSetting idlePitchDeadbandDeg = registerSetting(new DoubleSetting(
        "idle_pitch_deadband_deg", "Idle pitch deadband", 3.0, 0.0, 30.0));
    public final DoubleSetting activeYawRetargetDeg = registerSetting(new DoubleSetting(
        "active_yaw_retarget_deg", "Active yaw retarget", 6.0, 0.0, 45.0));
    public final DoubleSetting activePitchRetargetDeg = registerSetting(new DoubleSetting(
        "active_pitch_retarget_deg", "Active pitch retarget", 5.0, 0.0, 45.0));
    public final IntSetting rotationDurationMs = registerSetting(new IntSetting(
        "rotation_duration_ms", "Rotation duration ms", 550, 50, 3000));
    public final DoubleSetting pitchMinHorizontalDistance = registerSetting(new DoubleSetting(
        "pitch_min_horizontal_distance", "Pitch min horizontal distance", 2.25, 0.0, 12.0));
    public final DoubleSetting pitchLandDeadbandDeg = registerSetting(new DoubleSetting(
        "pitch_land_deadband_deg", "Pitch land deadband", 3.0, 0.0, 45.0));
    public final DoubleSetting pitchWaterDeadbandDeg = registerSetting(new DoubleSetting(
        "pitch_water_deadband_deg", "Pitch water deadband", 10.0, 0.0, 60.0));
    public final DoubleSetting pitchLandMaxAbsDeg = registerSetting(new DoubleSetting(
        "pitch_land_max_abs_deg", "Pitch land max abs", 22.0, 0.0, 89.0));
    public final DoubleSetting pitchWaterMaxAbsDeg = registerSetting(new DoubleSetting(
        "pitch_water_max_abs_deg", "Pitch water max abs", 8.0, 0.0, 89.0));
    public final DoubleSetting pitchLandMaxStepDeg = registerSetting(new DoubleSetting(
        "pitch_land_max_step_deg", "Pitch land max step", 7.0, 0.0, 45.0));
    public final DoubleSetting pitchWaterMaxStepDeg = registerSetting(new DoubleSetting(
        "pitch_water_max_step_deg", "Pitch water max step", 3.0, 0.0, 45.0));
    public final DoubleSetting pitchSnapToNeutralDeg = registerSetting(new DoubleSetting(
        "pitch_snap_to_neutral_deg", "Pitch snap neutral", 1.0, 0.0, 15.0));
    public final IntSetting cameraLookahead = registerSetting(new IntSetting(
        "camera_lookahead", "Camera lookahead nodes", 32, 1, 128));
    public final DoubleSetting cameraMaxLateralDev = registerSetting(new DoubleSetting(
        "camera_max_lateral_dev", "Camera max lateral dev", 2.5, 0.1, 12.0));
    public final IntSetting smoothOpenSkipBudget = registerSetting(new IntSetting(
        "smooth_open_skip_budget", "Smooth open skip budget", 12, 1, 64));
    public final IntSetting smoothMidSkipBudget = registerSetting(new IntSetting(
        "smooth_mid_skip_budget", "Smooth mid skip budget", 7, 1, 64));
    public final IntSetting smoothTightSkipBudget = registerSetting(new IntSetting(
        "smooth_tight_skip_budget", "Smooth tight skip budget", 4, 1, 64));
    public final IntSetting smoothOpenWallScoreMax = registerSetting(new IntSetting(
        "smooth_open_wall_score_max", "Smooth open wall score", 2, 0, 16));
    public final IntSetting smoothMidWallScoreMax = registerSetting(new IntSetting(
        "smooth_mid_wall_score_max", "Smooth mid wall score", 6, 0, 16));
    public final DoubleSetting smoothConstrainedCornerAngleDeg = registerSetting(new DoubleSetting(
        "smooth_constrained_corner_angle_deg", "Smooth corner angle", 35.0, 0.0, 180.0));
    public final DoubleSetting intermediateSpacing = registerSetting(new DoubleSetting(
        "intermediate_spacing", "Intermediate spacing", 4.0, 1.0, 16.0));
    public final DoubleSetting partialAscentThreshold = registerSetting(new DoubleSetting(
        "partial_ascent_threshold", "Partial ascent threshold", 0.2, -1.0, 2.0));
    public final DoubleSetting descentThreshold = registerSetting(new DoubleSetting(
        "descent_threshold", "Descent threshold", -0.1, -2.0, 1.0));
    public final DoubleSetting hugeDeviationHorizontalDistance = registerSetting(new DoubleSetting(
        "huge_deviation_horizontal_distance", "Huge deviation distance", 12.0, 1.0, 64.0));
    public final DoubleSetting smartCutoffMaxHorizontalDistance = registerSetting(new DoubleSetting(
        "smart_cutoff_max_horizontal_distance", "Smart cutoff max horizontal", 1.35, 0.0, 8.0));
    public final DoubleSetting smartCutoffMaxVerticalDistance = registerSetting(new DoubleSetting(
        "smart_cutoff_max_vertical_distance", "Smart cutoff max vertical", 1.75, 0.0, 8.0));
    public final DoubleSetting smartCutoffMinProgressSkip = registerSetting(new DoubleSetting(
        "smart_cutoff_min_progress_skip", "Smart cutoff progress skip", 1.25, 0.0, 8.0));
    public final IntSetting sustainedOffPathMaxMs = registerSetting(new IntSetting(
        "sustained_off_path_max_ms", "Off-path timeout ms", 5000, 100, 30000));
    public final IntSetting sustainedVerticalOffPathMaxMs = registerSetting(new IntSetting(
        "sustained_vertical_off_path_max_ms", "Vertical off-path timeout ms", 7000, 100, 30000));
    public final DoubleSetting offPathHorizontalDistance = registerSetting(new DoubleSetting(
        "off_path_horizontal_distance", "Off-path horizontal distance", 3.0, 0.1, 16.0));
    public final DoubleSetting offPathVerticalDistance = registerSetting(new DoubleSetting(
        "off_path_vertical_distance", "Off-path vertical distance", 3.0, 0.1, 16.0));
    public final IntSetting anomalousMinSegmentSkip = registerSetting(new IntSetting(
        "anomalous_min_segment_skip", "Anomalous min segment skip", 3, 1, 32));
    public final DoubleSetting anomalousMaxHorizontalDistance = registerSetting(new DoubleSetting(
        "anomalous_max_horizontal_distance", "Anomalous max horizontal", 5.5, 0.1, 32.0));
    public final DoubleSetting anomalousMaxVerticalDistance = registerSetting(new DoubleSetting(
        "anomalous_max_vertical_distance", "Anomalous max vertical", 4.5, 0.1, 32.0));
    public final DoubleSetting anomalousMinNearestAdvantage = registerSetting(new DoubleSetting(
        "anomalous_min_nearest_advantage", "Anomalous nearest advantage", 1.25, 0.0, 8.0));

    private PathfinderSettings() {
    }

    public static PathfinderSettings instance() {
        return INSTANCE;
    }

    public static List<Setting<?>> all() {
        syncFromConfig();
        return INSTANCE.settings();
    }

    public static List<Setting<?>> generalSettings() {
        syncFromConfig();
        return List.of(INSTANCE.showDebug, INSTANCE.maxJumpHeight);
    }

    public static List<Setting<?>> movementCostSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.walkCost,
            INSTANCE.diagonalCost,
            INSTANCE.fullStepAscentBaseCost,
            INSTANCE.fullStepAscentDyCost,
            INSTANCE.partialAscentCost,
            INSTANCE.jumpCost,
            INSTANCE.parkourCost,
            INSTANCE.fallDyCost,
            INSTANCE.swimCost,
            INSTANCE.climbCost);
    }

    public static List<Setting<?>> corridorCostSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.cardinalWallCost,
            INSTANCE.diagonalWallCost,
            INSTANCE.partialAscentEdgeCost,
            INSTANCE.partialAscentEntrySideCost,
            INSTANCE.openingEntryImbalanceCost);
    }

    public static List<Setting<?>> steeringSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.cornerSteeringEnabled,
            INSTANCE.cornerSteeringSlowdown,
            INSTANCE.cornerSteeringScanRadius,
            INSTANCE.cornerSteeringNudgeWeight,
            INSTANCE.cornerSteeringCenterlineWeight,
            INSTANCE.cornerSteeringCenterlineStart,
            INSTANCE.cornerSteeringCenterlineMax);
    }

    public static List<Setting<?>> planningLimitSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.defaultWalkMaxIterations,
            INSTANCE.defaultWalkMaxLength,
            INSTANCE.instantWalkMaxIterations,
            INSTANCE.instantWalkMaxLength,
            INSTANCE.repairWalkMaxIterations,
            INSTANCE.repairWalkMaxLength,
            INSTANCE.queuedLongRangeMaxIterations,
            INSTANCE.queuedLongRangeMaxLength);
    }

    public static List<Setting<?>> executionSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.stuckDistThreshold,
            INSTANCE.stuckTimeMs,
            INSTANCE.driftDistance,
            INSTANCE.replanCooldownMs,
            INSTANCE.jumpTriggerDist,
            INSTANCE.stepUpTriggerDist,
            INSTANCE.jumpCooldownTicks,
            INSTANCE.stallJumpProgressMs,
            INSTANCE.pathProgressEpsilon,
            INSTANCE.walkTargetDeadzone,
            INSTANCE.walkForwardDot,
            INSTANCE.walkBackwardDot,
            INSTANCE.walkStrafeDot,
            INSTANCE.coastTimeoutMs,
            INSTANCE.smartCutoffCooldownMs,
            INSTANCE.localRepairLookahead,
            INSTANCE.localRepairDriftLookahead,
            INSTANCE.localRepairDriftThreshold,
            INSTANCE.goalReachedHDist,
            INSTANCE.goalReachedVDist);
    }

    public static List<Setting<?>> recoverySettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.unstuckJumpMs,
            INSTANCE.unstuckBackupMs,
            INSTANCE.backupTicks,
            INSTANCE.pathReplanStaleMs,
            INSTANCE.pathReplanDriftDistance,
            INSTANCE.groundedNoProgressReplanMs,
            INSTANCE.pathReplanHardStaleMs,
            INSTANCE.backupMaxHorizontalSpeed,
            INSTANCE.cornerAlignMinDistance,
            INSTANCE.cornerAlignMaxDistance,
            INSTANCE.cornerAlignMaxVertical,
            INSTANCE.cornerAlignMaxMs);
    }

    public static List<Setting<?>> longRangeSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.segmentRecalcRatio,
            INSTANCE.earlySegmentRecalcRatio,
            INSTANCE.horizonTrimRatio,
            INSTANCE.prepareRemainingDistance,
            INSTANCE.emergencyRemainingDistance,
            INSTANCE.preparedSwitchRemainingDistance,
            INSTANCE.finalGoalXzTolerance,
            INSTANCE.maxSegmentDistance,
            INSTANCE.segmentRetryCooldownMs,
            INSTANCE.playerStartAfterFailures,
            INSTANCE.playerStartRecoveryRatio);
    }

    public static List<Setting<?>> rotationSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.useCameraRail,
            INSTANCE.cameraRailReachedDist,
            INSTANCE.legacyCameraEyeY,
            INSTANCE.cameraRailGuideLookaheadDist,
            INSTANCE.rotationRedispatchCooldownMs,
            INSTANCE.idleYawDeadbandDeg,
            INSTANCE.idlePitchDeadbandDeg,
            INSTANCE.activeYawRetargetDeg,
            INSTANCE.activePitchRetargetDeg,
            INSTANCE.rotationDurationMs,
            INSTANCE.pitchMinHorizontalDistance,
            INSTANCE.pitchLandDeadbandDeg,
            INSTANCE.pitchWaterDeadbandDeg,
            INSTANCE.pitchLandMaxAbsDeg,
            INSTANCE.pitchWaterMaxAbsDeg,
            INSTANCE.pitchLandMaxStepDeg,
            INSTANCE.pitchWaterMaxStepDeg,
            INSTANCE.pitchSnapToNeutralDeg,
            INSTANCE.cameraLookahead,
            INSTANCE.cameraMaxLateralDev);
    }

    public static List<Setting<?>> smoothingSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.smoothOpenSkipBudget,
            INSTANCE.smoothMidSkipBudget,
            INSTANCE.smoothTightSkipBudget,
            INSTANCE.smoothOpenWallScoreMax,
            INSTANCE.smoothMidWallScoreMax,
            INSTANCE.smoothConstrainedCornerAngleDeg,
            INSTANCE.intermediateSpacing,
            INSTANCE.partialAscentThreshold,
            INSTANCE.descentThreshold);
    }

    public static List<Setting<?>> pathCheckSettings() {
        syncFromConfig();
        return List.of(
            INSTANCE.hugeDeviationHorizontalDistance,
            INSTANCE.smartCutoffMaxHorizontalDistance,
            INSTANCE.smartCutoffMaxVerticalDistance,
            INSTANCE.smartCutoffMinProgressSkip,
            INSTANCE.sustainedOffPathMaxMs,
            INSTANCE.sustainedVerticalOffPathMaxMs,
            INSTANCE.offPathHorizontalDistance,
            INSTANCE.offPathVerticalDistance,
            INSTANCE.anomalousMinSegmentSkip,
            INSTANCE.anomalousMaxHorizontalDistance,
            INSTANCE.anomalousMaxVerticalDistance,
            INSTANCE.anomalousMinNearestAdvantage);
    }

    public static void apply() {
        PathfinderConfig.SHOW_DEBUG.set(INSTANCE.showDebug.value());
        PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.set(INSTANCE.maxJumpHeight.value());
        PathfinderConfig.WALK_COST.set(INSTANCE.walkCost.value());
        PathfinderConfig.DIAGONAL_COST.set(INSTANCE.diagonalCost.value());
        PathfinderConfig.FULL_STEP_ASCENT_BASE_COST.set(INSTANCE.fullStepAscentBaseCost.value());
        PathfinderConfig.FULL_STEP_ASCENT_DY_COST.set(INSTANCE.fullStepAscentDyCost.value());
        PathfinderConfig.PARTIAL_ASCENT_COST.set(INSTANCE.partialAscentCost.value());
        PathfinderConfig.JUMP_COST.set(INSTANCE.jumpCost.value());
        PathfinderConfig.PARKOUR_COST.set(INSTANCE.parkourCost.value());
        PathfinderConfig.FALL_DY_COST.set(INSTANCE.fallDyCost.value());
        PathfinderConfig.SWIM_COST.set(INSTANCE.swimCost.value());
        PathfinderConfig.CLIMB_COST.set(INSTANCE.climbCost.value());
        PathfinderConfig.CARDINAL_WALL_COST.set(INSTANCE.cardinalWallCost.value());
        PathfinderConfig.DIAGONAL_WALL_COST.set(INSTANCE.diagonalWallCost.value());
        PathfinderConfig.PARTIAL_ASCENT_EDGE_COST.set(INSTANCE.partialAscentEdgeCost.value());
        PathfinderConfig.PARTIAL_ASCENT_ENTRY_SIDE_COST.set(INSTANCE.partialAscentEntrySideCost.value());
        PathfinderConfig.OPENING_ENTRY_IMBALANCE_COST.set(INSTANCE.openingEntryImbalanceCost.value());
        PathfinderConfig.CORNER_STEERING_ENABLED.set(INSTANCE.cornerSteeringEnabled.value());
        PathfinderConfig.CORNER_STEERING_SLOWDOWN.set(INSTANCE.cornerSteeringSlowdown.value());
        PathfinderConfig.CORNER_STEERING_SCAN_RADIUS.set(INSTANCE.cornerSteeringScanRadius.value());
        PathfinderConfig.CORNER_STEERING_NUDGE_WEIGHT.set(INSTANCE.cornerSteeringNudgeWeight.value());
        PathfinderConfig.CORNER_STEERING_CENTERLINE_WEIGHT.set(INSTANCE.cornerSteeringCenterlineWeight.value());
        PathfinderConfig.CORNER_STEERING_CENTERLINE_START.set(INSTANCE.cornerSteeringCenterlineStart.value());
        PathfinderConfig.CORNER_STEERING_CENTERLINE_MAX.set(INSTANCE.cornerSteeringCenterlineMax.value());
        PathfinderConfig.DEFAULT_WALK_MAX_ITERATIONS.set(INSTANCE.defaultWalkMaxIterations.value());
        PathfinderConfig.DEFAULT_WALK_MAX_LENGTH.set(INSTANCE.defaultWalkMaxLength.value());
        PathfinderConfig.INSTANT_WALK_MAX_ITERATIONS.set(INSTANCE.instantWalkMaxIterations.value());
        PathfinderConfig.INSTANT_WALK_MAX_LENGTH.set(INSTANCE.instantWalkMaxLength.value());
        PathfinderConfig.REPAIR_WALK_MAX_ITERATIONS.set(INSTANCE.repairWalkMaxIterations.value());
        PathfinderConfig.REPAIR_WALK_MAX_LENGTH.set(INSTANCE.repairWalkMaxLength.value());
        PathfinderConfig.QUEUED_LONG_RANGE_MAX_ITERATIONS.set(INSTANCE.queuedLongRangeMaxIterations.value());
        PathfinderConfig.QUEUED_LONG_RANGE_MAX_LENGTH.set(INSTANCE.queuedLongRangeMaxLength.value());
        PathfinderConfig.STUCK_DIST_THRESHOLD.set(INSTANCE.stuckDistThreshold.value());
        PathfinderConfig.STUCK_TIME_MS.set(INSTANCE.stuckTimeMs.value());
        PathfinderConfig.DRIFT_DISTANCE.set(INSTANCE.driftDistance.value());
        PathfinderConfig.REPLAN_COOLDOWN_MS.set(INSTANCE.replanCooldownMs.value());
        PathfinderConfig.JUMP_TRIGGER_DIST.set(INSTANCE.jumpTriggerDist.value());
        PathfinderConfig.STEP_UP_TRIGGER_DIST.set(INSTANCE.stepUpTriggerDist.value());
        PathfinderConfig.JUMP_COOLDOWN_TICKS.set(INSTANCE.jumpCooldownTicks.value());
        PathfinderConfig.STALL_JUMP_PROGRESS_MS.set(INSTANCE.stallJumpProgressMs.value());
        PathfinderConfig.PATH_PROGRESS_EPSILON.set(INSTANCE.pathProgressEpsilon.value());
        PathfinderConfig.WALK_TARGET_DEADZONE.set(INSTANCE.walkTargetDeadzone.value());
        PathfinderConfig.WALK_FORWARD_DOT.set(INSTANCE.walkForwardDot.value());
        PathfinderConfig.WALK_BACKWARD_DOT.set(INSTANCE.walkBackwardDot.value());
        PathfinderConfig.WALK_STRAFE_DOT.set(INSTANCE.walkStrafeDot.value());
        PathfinderConfig.COAST_TIMEOUT_MS.set(INSTANCE.coastTimeoutMs.value());
        PathfinderConfig.SMART_CUTOFF_COOLDOWN_MS.set(INSTANCE.smartCutoffCooldownMs.value());
        PathfinderConfig.LOCAL_REPAIR_LOOKAHEAD.set(INSTANCE.localRepairLookahead.value());
        PathfinderConfig.LOCAL_REPAIR_DRIFT_LOOKAHEAD.set(INSTANCE.localRepairDriftLookahead.value());
        PathfinderConfig.LOCAL_REPAIR_DRIFT_THRESHOLD.set(INSTANCE.localRepairDriftThreshold.value());
        PathfinderConfig.GOAL_REACHED_HDIST.set(INSTANCE.goalReachedHDist.value());
        PathfinderConfig.GOAL_REACHED_VDIST.set(INSTANCE.goalReachedVDist.value());
        PathfinderConfig.UNSTUCK_JUMP_MS.set(INSTANCE.unstuckJumpMs.value());
        PathfinderConfig.UNSTUCK_BACKUP_MS.set(INSTANCE.unstuckBackupMs.value());
        PathfinderConfig.BACKUP_TICKS.set(INSTANCE.backupTicks.value());
        PathfinderConfig.PATH_REPLAN_STALE_MS.set(INSTANCE.pathReplanStaleMs.value());
        PathfinderConfig.PATH_REPLAN_DRIFT_DISTANCE.set(INSTANCE.pathReplanDriftDistance.value());
        PathfinderConfig.GROUNDED_NO_PROGRESS_REPLAN_MS.set(INSTANCE.groundedNoProgressReplanMs.value());
        PathfinderConfig.PATH_REPLAN_HARD_STALE_MS.set(INSTANCE.pathReplanHardStaleMs.value());
        PathfinderConfig.BACKUP_MAX_HORIZONTAL_SPEED.set(INSTANCE.backupMaxHorizontalSpeed.value());
        PathfinderConfig.CORNER_ALIGN_MIN_DISTANCE.set(INSTANCE.cornerAlignMinDistance.value());
        PathfinderConfig.CORNER_ALIGN_MAX_DISTANCE.set(INSTANCE.cornerAlignMaxDistance.value());
        PathfinderConfig.CORNER_ALIGN_MAX_VERTICAL.set(INSTANCE.cornerAlignMaxVertical.value());
        PathfinderConfig.CORNER_ALIGN_MAX_MS.set(INSTANCE.cornerAlignMaxMs.value());
        PathfinderConfig.SEGMENT_RECALC_RATIO.set(INSTANCE.segmentRecalcRatio.value());
        PathfinderConfig.EARLY_SEGMENT_RECALC_RATIO.set(INSTANCE.earlySegmentRecalcRatio.value());
        PathfinderConfig.HORIZON_TRIM_RATIO.set(INSTANCE.horizonTrimRatio.value());
        PathfinderConfig.PREPARE_REMAINING_DISTANCE.set(INSTANCE.prepareRemainingDistance.value());
        PathfinderConfig.EMERGENCY_REMAINING_DISTANCE.set(INSTANCE.emergencyRemainingDistance.value());
        PathfinderConfig.PREPARED_SWITCH_REMAINING_DISTANCE.set(INSTANCE.preparedSwitchRemainingDistance.value());
        PathfinderConfig.FINAL_GOAL_XZ_TOLERANCE.set(INSTANCE.finalGoalXzTolerance.value());
        PathfinderConfig.MAX_SEGMENT_DISTANCE.set(INSTANCE.maxSegmentDistance.value());
        PathfinderConfig.SEGMENT_RETRY_COOLDOWN_MS.set(INSTANCE.segmentRetryCooldownMs.value());
        PathfinderConfig.PLAYER_START_AFTER_FAILURES.set(INSTANCE.playerStartAfterFailures.value());
        PathfinderConfig.PLAYER_START_RECOVERY_RATIO.set(INSTANCE.playerStartRecoveryRatio.value());
        PathfinderConfig.USE_CAMERA_RAIL.set(INSTANCE.useCameraRail.value());
        PathfinderConfig.CAMERA_RAIL_REACHED_DIST.set(INSTANCE.cameraRailReachedDist.value());
        PathfinderConfig.LEGACY_CAMERA_EYE_Y.set(INSTANCE.legacyCameraEyeY.value());
        PathfinderConfig.CAMERA_RAIL_GUIDE_LOOKAHEAD_DIST.set(INSTANCE.cameraRailGuideLookaheadDist.value());
        PathfinderConfig.ROTATION_REDISPATCH_COOLDOWN_MS.set(INSTANCE.rotationRedispatchCooldownMs.value());
        PathfinderConfig.IDLE_YAW_DEADBAND_DEG.set(INSTANCE.idleYawDeadbandDeg.value());
        PathfinderConfig.IDLE_PITCH_DEADBAND_DEG.set(INSTANCE.idlePitchDeadbandDeg.value());
        PathfinderConfig.ACTIVE_YAW_RETARGET_DEG.set(INSTANCE.activeYawRetargetDeg.value());
        PathfinderConfig.ACTIVE_PITCH_RETARGET_DEG.set(INSTANCE.activePitchRetargetDeg.value());
        PathfinderConfig.ROTATION_DURATION_MS.set(INSTANCE.rotationDurationMs.value());
        PathfinderConfig.PITCH_MIN_HORIZONTAL_DISTANCE.set(INSTANCE.pitchMinHorizontalDistance.value());
        PathfinderConfig.PITCH_LAND_DEADBAND_DEG.set(INSTANCE.pitchLandDeadbandDeg.value());
        PathfinderConfig.PITCH_WATER_DEADBAND_DEG.set(INSTANCE.pitchWaterDeadbandDeg.value());
        PathfinderConfig.PITCH_LAND_MAX_ABS_DEG.set(INSTANCE.pitchLandMaxAbsDeg.value());
        PathfinderConfig.PITCH_WATER_MAX_ABS_DEG.set(INSTANCE.pitchWaterMaxAbsDeg.value());
        PathfinderConfig.PITCH_LAND_MAX_STEP_DEG.set(INSTANCE.pitchLandMaxStepDeg.value());
        PathfinderConfig.PITCH_WATER_MAX_STEP_DEG.set(INSTANCE.pitchWaterMaxStepDeg.value());
        PathfinderConfig.PITCH_SNAP_TO_NEUTRAL_DEG.set(INSTANCE.pitchSnapToNeutralDeg.value());
        PathfinderConfig.CAMERA_LOOKAHEAD.set(INSTANCE.cameraLookahead.value());
        PathfinderConfig.CAMERA_MAX_LATERAL_DEV.set(INSTANCE.cameraMaxLateralDev.value());
        PathfinderConfig.SMOOTH_OPEN_SKIP_BUDGET.set(INSTANCE.smoothOpenSkipBudget.value());
        PathfinderConfig.SMOOTH_MID_SKIP_BUDGET.set(INSTANCE.smoothMidSkipBudget.value());
        PathfinderConfig.SMOOTH_TIGHT_SKIP_BUDGET.set(INSTANCE.smoothTightSkipBudget.value());
        PathfinderConfig.SMOOTH_OPEN_WALL_SCORE_MAX.set(INSTANCE.smoothOpenWallScoreMax.value());
        PathfinderConfig.SMOOTH_MID_WALL_SCORE_MAX.set(INSTANCE.smoothMidWallScoreMax.value());
        PathfinderConfig.SMOOTH_CONSTRAINED_CORNER_ANGLE_DEG.set(INSTANCE.smoothConstrainedCornerAngleDeg.value());
        PathfinderConfig.INTERMEDIATE_SPACING.set(INSTANCE.intermediateSpacing.value());
        PathfinderConfig.PARTIAL_ASCENT_THRESHOLD.set(INSTANCE.partialAscentThreshold.value());
        PathfinderConfig.DESCENT_THRESHOLD.set(INSTANCE.descentThreshold.value());
        PathfinderConfig.HUGE_DEVIATION_HORIZONTAL_DISTANCE.set(INSTANCE.hugeDeviationHorizontalDistance.value());
        PathfinderConfig.SMART_CUTOFF_MAX_HORIZONTAL_DISTANCE.set(INSTANCE.smartCutoffMaxHorizontalDistance.value());
        PathfinderConfig.SMART_CUTOFF_MAX_VERTICAL_DISTANCE.set(INSTANCE.smartCutoffMaxVerticalDistance.value());
        PathfinderConfig.SMART_CUTOFF_MIN_PROGRESS_SKIP.set(INSTANCE.smartCutoffMinProgressSkip.value());
        PathfinderConfig.SUSTAINED_OFF_PATH_MAX_MS.set(INSTANCE.sustainedOffPathMaxMs.value());
        PathfinderConfig.SUSTAINED_VERTICAL_OFF_PATH_MAX_MS.set(INSTANCE.sustainedVerticalOffPathMaxMs.value());
        PathfinderConfig.OFF_PATH_HORIZONTAL_DISTANCE.set(INSTANCE.offPathHorizontalDistance.value());
        PathfinderConfig.OFF_PATH_VERTICAL_DISTANCE.set(INSTANCE.offPathVerticalDistance.value());
        PathfinderConfig.ANOMALOUS_MIN_SEGMENT_SKIP.set(INSTANCE.anomalousMinSegmentSkip.value());
        PathfinderConfig.ANOMALOUS_MAX_HORIZONTAL_DISTANCE.set(INSTANCE.anomalousMaxHorizontalDistance.value());
        PathfinderConfig.ANOMALOUS_MAX_VERTICAL_DISTANCE.set(INSTANCE.anomalousMaxVerticalDistance.value());
        PathfinderConfig.ANOMALOUS_MIN_NEAREST_ADVANTAGE.set(INSTANCE.anomalousMinNearestAdvantage.value());
    }

    public static void resetToDefaults() {
        INSTANCE.resetSettings();
        apply();
    }

    private static void syncFromConfig() {
        INSTANCE.showDebug.setValue(PathfinderConfig.SHOW_DEBUG.get());
        INSTANCE.maxJumpHeight.setValue(PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get());
        INSTANCE.walkCost.setValue(PathfinderConfig.WALK_COST.get());
        INSTANCE.diagonalCost.setValue(PathfinderConfig.DIAGONAL_COST.get());
        INSTANCE.fullStepAscentBaseCost.setValue(PathfinderConfig.FULL_STEP_ASCENT_BASE_COST.get());
        INSTANCE.fullStepAscentDyCost.setValue(PathfinderConfig.FULL_STEP_ASCENT_DY_COST.get());
        INSTANCE.partialAscentCost.setValue(PathfinderConfig.PARTIAL_ASCENT_COST.get());
        INSTANCE.jumpCost.setValue(PathfinderConfig.JUMP_COST.get());
        INSTANCE.parkourCost.setValue(PathfinderConfig.PARKOUR_COST.get());
        INSTANCE.fallDyCost.setValue(PathfinderConfig.FALL_DY_COST.get());
        INSTANCE.swimCost.setValue(PathfinderConfig.SWIM_COST.get());
        INSTANCE.climbCost.setValue(PathfinderConfig.CLIMB_COST.get());
        INSTANCE.cardinalWallCost.setValue(PathfinderConfig.CARDINAL_WALL_COST.get());
        INSTANCE.diagonalWallCost.setValue(PathfinderConfig.DIAGONAL_WALL_COST.get());
        INSTANCE.partialAscentEdgeCost.setValue(PathfinderConfig.PARTIAL_ASCENT_EDGE_COST.get());
        INSTANCE.partialAscentEntrySideCost.setValue(PathfinderConfig.PARTIAL_ASCENT_ENTRY_SIDE_COST.get());
        INSTANCE.openingEntryImbalanceCost.setValue(PathfinderConfig.OPENING_ENTRY_IMBALANCE_COST.get());
        INSTANCE.cornerSteeringEnabled.setValue(PathfinderConfig.CORNER_STEERING_ENABLED.get());
        INSTANCE.cornerSteeringSlowdown.setValue(PathfinderConfig.CORNER_STEERING_SLOWDOWN.get());
        INSTANCE.cornerSteeringScanRadius.setValue(PathfinderConfig.CORNER_STEERING_SCAN_RADIUS.get());
        INSTANCE.cornerSteeringNudgeWeight.setValue(PathfinderConfig.CORNER_STEERING_NUDGE_WEIGHT.get());
        INSTANCE.cornerSteeringCenterlineWeight.setValue(PathfinderConfig.CORNER_STEERING_CENTERLINE_WEIGHT.get());
        INSTANCE.cornerSteeringCenterlineStart.setValue(PathfinderConfig.CORNER_STEERING_CENTERLINE_START.get());
        INSTANCE.cornerSteeringCenterlineMax.setValue(PathfinderConfig.CORNER_STEERING_CENTERLINE_MAX.get());
        INSTANCE.defaultWalkMaxIterations.setValue(PathfinderConfig.DEFAULT_WALK_MAX_ITERATIONS.get());
        INSTANCE.defaultWalkMaxLength.setValue(PathfinderConfig.DEFAULT_WALK_MAX_LENGTH.get());
        INSTANCE.instantWalkMaxIterations.setValue(PathfinderConfig.INSTANT_WALK_MAX_ITERATIONS.get());
        INSTANCE.instantWalkMaxLength.setValue(PathfinderConfig.INSTANT_WALK_MAX_LENGTH.get());
        INSTANCE.repairWalkMaxIterations.setValue(PathfinderConfig.REPAIR_WALK_MAX_ITERATIONS.get());
        INSTANCE.repairWalkMaxLength.setValue(PathfinderConfig.REPAIR_WALK_MAX_LENGTH.get());
        INSTANCE.queuedLongRangeMaxIterations.setValue(PathfinderConfig.QUEUED_LONG_RANGE_MAX_ITERATIONS.get());
        INSTANCE.queuedLongRangeMaxLength.setValue(PathfinderConfig.QUEUED_LONG_RANGE_MAX_LENGTH.get());
        INSTANCE.stuckDistThreshold.setValue(PathfinderConfig.STUCK_DIST_THRESHOLD.get());
        INSTANCE.stuckTimeMs.setValue(PathfinderConfig.STUCK_TIME_MS.get());
        INSTANCE.driftDistance.setValue(PathfinderConfig.DRIFT_DISTANCE.get());
        INSTANCE.replanCooldownMs.setValue(PathfinderConfig.REPLAN_COOLDOWN_MS.get());
        INSTANCE.jumpTriggerDist.setValue(PathfinderConfig.JUMP_TRIGGER_DIST.get());
        INSTANCE.stepUpTriggerDist.setValue(PathfinderConfig.STEP_UP_TRIGGER_DIST.get());
        INSTANCE.jumpCooldownTicks.setValue(PathfinderConfig.JUMP_COOLDOWN_TICKS.get());
        INSTANCE.stallJumpProgressMs.setValue(PathfinderConfig.STALL_JUMP_PROGRESS_MS.get());
        INSTANCE.pathProgressEpsilon.setValue(PathfinderConfig.PATH_PROGRESS_EPSILON.get());
        INSTANCE.walkTargetDeadzone.setValue(PathfinderConfig.WALK_TARGET_DEADZONE.get());
        INSTANCE.walkForwardDot.setValue(PathfinderConfig.WALK_FORWARD_DOT.get());
        INSTANCE.walkBackwardDot.setValue(PathfinderConfig.WALK_BACKWARD_DOT.get());
        INSTANCE.walkStrafeDot.setValue(PathfinderConfig.WALK_STRAFE_DOT.get());
        INSTANCE.coastTimeoutMs.setValue(PathfinderConfig.COAST_TIMEOUT_MS.get());
        INSTANCE.smartCutoffCooldownMs.setValue(PathfinderConfig.SMART_CUTOFF_COOLDOWN_MS.get());
        INSTANCE.localRepairLookahead.setValue(PathfinderConfig.LOCAL_REPAIR_LOOKAHEAD.get());
        INSTANCE.localRepairDriftLookahead.setValue(PathfinderConfig.LOCAL_REPAIR_DRIFT_LOOKAHEAD.get());
        INSTANCE.localRepairDriftThreshold.setValue(PathfinderConfig.LOCAL_REPAIR_DRIFT_THRESHOLD.get());
        INSTANCE.goalReachedHDist.setValue(PathfinderConfig.GOAL_REACHED_HDIST.get());
        INSTANCE.goalReachedVDist.setValue(PathfinderConfig.GOAL_REACHED_VDIST.get());
        INSTANCE.unstuckJumpMs.setValue(PathfinderConfig.UNSTUCK_JUMP_MS.get());
        INSTANCE.unstuckBackupMs.setValue(PathfinderConfig.UNSTUCK_BACKUP_MS.get());
        INSTANCE.backupTicks.setValue(PathfinderConfig.BACKUP_TICKS.get());
        INSTANCE.pathReplanStaleMs.setValue(PathfinderConfig.PATH_REPLAN_STALE_MS.get());
        INSTANCE.pathReplanDriftDistance.setValue(PathfinderConfig.PATH_REPLAN_DRIFT_DISTANCE.get());
        INSTANCE.groundedNoProgressReplanMs.setValue(PathfinderConfig.GROUNDED_NO_PROGRESS_REPLAN_MS.get());
        INSTANCE.pathReplanHardStaleMs.setValue(PathfinderConfig.PATH_REPLAN_HARD_STALE_MS.get());
        INSTANCE.backupMaxHorizontalSpeed.setValue(PathfinderConfig.BACKUP_MAX_HORIZONTAL_SPEED.get());
        INSTANCE.cornerAlignMinDistance.setValue(PathfinderConfig.CORNER_ALIGN_MIN_DISTANCE.get());
        INSTANCE.cornerAlignMaxDistance.setValue(PathfinderConfig.CORNER_ALIGN_MAX_DISTANCE.get());
        INSTANCE.cornerAlignMaxVertical.setValue(PathfinderConfig.CORNER_ALIGN_MAX_VERTICAL.get());
        INSTANCE.cornerAlignMaxMs.setValue(PathfinderConfig.CORNER_ALIGN_MAX_MS.get());
        INSTANCE.segmentRecalcRatio.setValue(PathfinderConfig.SEGMENT_RECALC_RATIO.get());
        INSTANCE.earlySegmentRecalcRatio.setValue(PathfinderConfig.EARLY_SEGMENT_RECALC_RATIO.get());
        INSTANCE.horizonTrimRatio.setValue(PathfinderConfig.HORIZON_TRIM_RATIO.get());
        INSTANCE.prepareRemainingDistance.setValue(PathfinderConfig.PREPARE_REMAINING_DISTANCE.get());
        INSTANCE.emergencyRemainingDistance.setValue(PathfinderConfig.EMERGENCY_REMAINING_DISTANCE.get());
        INSTANCE.preparedSwitchRemainingDistance.setValue(PathfinderConfig.PREPARED_SWITCH_REMAINING_DISTANCE.get());
        INSTANCE.finalGoalXzTolerance.setValue(PathfinderConfig.FINAL_GOAL_XZ_TOLERANCE.get());
        INSTANCE.maxSegmentDistance.setValue(PathfinderConfig.MAX_SEGMENT_DISTANCE.get());
        INSTANCE.segmentRetryCooldownMs.setValue(PathfinderConfig.SEGMENT_RETRY_COOLDOWN_MS.get());
        INSTANCE.playerStartAfterFailures.setValue(PathfinderConfig.PLAYER_START_AFTER_FAILURES.get());
        INSTANCE.playerStartRecoveryRatio.setValue(PathfinderConfig.PLAYER_START_RECOVERY_RATIO.get());
        INSTANCE.useCameraRail.setValue(PathfinderConfig.USE_CAMERA_RAIL.get());
        INSTANCE.cameraRailReachedDist.setValue(PathfinderConfig.CAMERA_RAIL_REACHED_DIST.get());
        INSTANCE.legacyCameraEyeY.setValue(PathfinderConfig.LEGACY_CAMERA_EYE_Y.get());
        INSTANCE.cameraRailGuideLookaheadDist.setValue(PathfinderConfig.CAMERA_RAIL_GUIDE_LOOKAHEAD_DIST.get());
        INSTANCE.rotationRedispatchCooldownMs.setValue(PathfinderConfig.ROTATION_REDISPATCH_COOLDOWN_MS.get());
        INSTANCE.idleYawDeadbandDeg.setValue(PathfinderConfig.IDLE_YAW_DEADBAND_DEG.get());
        INSTANCE.idlePitchDeadbandDeg.setValue(PathfinderConfig.IDLE_PITCH_DEADBAND_DEG.get());
        INSTANCE.activeYawRetargetDeg.setValue(PathfinderConfig.ACTIVE_YAW_RETARGET_DEG.get());
        INSTANCE.activePitchRetargetDeg.setValue(PathfinderConfig.ACTIVE_PITCH_RETARGET_DEG.get());
        INSTANCE.rotationDurationMs.setValue(PathfinderConfig.ROTATION_DURATION_MS.get());
        INSTANCE.pitchMinHorizontalDistance.setValue(PathfinderConfig.PITCH_MIN_HORIZONTAL_DISTANCE.get());
        INSTANCE.pitchLandDeadbandDeg.setValue(PathfinderConfig.PITCH_LAND_DEADBAND_DEG.get());
        INSTANCE.pitchWaterDeadbandDeg.setValue(PathfinderConfig.PITCH_WATER_DEADBAND_DEG.get());
        INSTANCE.pitchLandMaxAbsDeg.setValue(PathfinderConfig.PITCH_LAND_MAX_ABS_DEG.get());
        INSTANCE.pitchWaterMaxAbsDeg.setValue(PathfinderConfig.PITCH_WATER_MAX_ABS_DEG.get());
        INSTANCE.pitchLandMaxStepDeg.setValue(PathfinderConfig.PITCH_LAND_MAX_STEP_DEG.get());
        INSTANCE.pitchWaterMaxStepDeg.setValue(PathfinderConfig.PITCH_WATER_MAX_STEP_DEG.get());
        INSTANCE.pitchSnapToNeutralDeg.setValue(PathfinderConfig.PITCH_SNAP_TO_NEUTRAL_DEG.get());
        INSTANCE.cameraLookahead.setValue(PathfinderConfig.CAMERA_LOOKAHEAD.get());
        INSTANCE.cameraMaxLateralDev.setValue(PathfinderConfig.CAMERA_MAX_LATERAL_DEV.get());
        INSTANCE.smoothOpenSkipBudget.setValue(PathfinderConfig.SMOOTH_OPEN_SKIP_BUDGET.get());
        INSTANCE.smoothMidSkipBudget.setValue(PathfinderConfig.SMOOTH_MID_SKIP_BUDGET.get());
        INSTANCE.smoothTightSkipBudget.setValue(PathfinderConfig.SMOOTH_TIGHT_SKIP_BUDGET.get());
        INSTANCE.smoothOpenWallScoreMax.setValue(PathfinderConfig.SMOOTH_OPEN_WALL_SCORE_MAX.get());
        INSTANCE.smoothMidWallScoreMax.setValue(PathfinderConfig.SMOOTH_MID_WALL_SCORE_MAX.get());
        INSTANCE.smoothConstrainedCornerAngleDeg.setValue(PathfinderConfig.SMOOTH_CONSTRAINED_CORNER_ANGLE_DEG.get());
        INSTANCE.intermediateSpacing.setValue(PathfinderConfig.INTERMEDIATE_SPACING.get());
        INSTANCE.partialAscentThreshold.setValue(PathfinderConfig.PARTIAL_ASCENT_THRESHOLD.get());
        INSTANCE.descentThreshold.setValue(PathfinderConfig.DESCENT_THRESHOLD.get());
        INSTANCE.hugeDeviationHorizontalDistance.setValue(PathfinderConfig.HUGE_DEVIATION_HORIZONTAL_DISTANCE.get());
        INSTANCE.smartCutoffMaxHorizontalDistance.setValue(PathfinderConfig.SMART_CUTOFF_MAX_HORIZONTAL_DISTANCE.get());
        INSTANCE.smartCutoffMaxVerticalDistance.setValue(PathfinderConfig.SMART_CUTOFF_MAX_VERTICAL_DISTANCE.get());
        INSTANCE.smartCutoffMinProgressSkip.setValue(PathfinderConfig.SMART_CUTOFF_MIN_PROGRESS_SKIP.get());
        INSTANCE.sustainedOffPathMaxMs.setValue(PathfinderConfig.SUSTAINED_OFF_PATH_MAX_MS.get());
        INSTANCE.sustainedVerticalOffPathMaxMs.setValue(PathfinderConfig.SUSTAINED_VERTICAL_OFF_PATH_MAX_MS.get());
        INSTANCE.offPathHorizontalDistance.setValue(PathfinderConfig.OFF_PATH_HORIZONTAL_DISTANCE.get());
        INSTANCE.offPathVerticalDistance.setValue(PathfinderConfig.OFF_PATH_VERTICAL_DISTANCE.get());
        INSTANCE.anomalousMinSegmentSkip.setValue(PathfinderConfig.ANOMALOUS_MIN_SEGMENT_SKIP.get());
        INSTANCE.anomalousMaxHorizontalDistance.setValue(PathfinderConfig.ANOMALOUS_MAX_HORIZONTAL_DISTANCE.get());
        INSTANCE.anomalousMaxVerticalDistance.setValue(PathfinderConfig.ANOMALOUS_MAX_VERTICAL_DISTANCE.get());
        INSTANCE.anomalousMinNearestAdvantage.setValue(PathfinderConfig.ANOMALOUS_MIN_NEAREST_ADVANTAGE.get());
    }
}
