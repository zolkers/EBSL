package fr.riege.ebsl.common.pathfinding.settings;

import fr.riege.ebsl.common.core.settings.*;

import java.util.List;

public final class PathfinderSettings extends Settingable {
    private static final PathfinderSettings INSTANCE = new PathfinderSettings();
    public final BooleanSetting showDebug = registerSetting(new BooleanSetting("show_debug", "Show debug", true));
    public final IntSetting maxJumpHeight = registerSetting(new IntSetting("max_jump_height", "Max jump height", 1, 1, 20));
    public final BooleanSetting renderPathNodes = registerSetting(new BooleanSetting(
        "render_path_nodes", "Render path nodes", true));
    public final BooleanSetting renderPathLines = registerSetting(new BooleanSetting(
        "render_path_lines", "Render path lines", true));
    public final BooleanSetting renderCameraRail = registerSetting(new BooleanSetting(
        "render_camera_rail", "Render camera rail", true));
    public final IntSetting renderMaxPathNodes = registerSetting(new IntSetting(
        "render_max_path_nodes", "Max rendered path nodes", 300, 1, 4000));
    public final IntSetting renderMaxCameraNodes = registerSetting(new IntSetting(
        "render_max_camera_nodes", "Max rendered camera nodes", 480, 1, 4000));
    public final DoubleSetting renderPathLineWidth = registerSetting(new DoubleSetting(
        "render_path_line_width", "Path line width", 2.0, 0.25, 8.0));
    public final DoubleSetting renderCameraLineWidth = registerSetting(new DoubleSetting(
        "render_camera_line_width", "Camera rail line width", 1.25, 0.25, 8.0));
    public final EnumSetting<PathRenderColorMode> renderPathColorMode = registerSetting(new EnumSetting<>(
        "render_path_color_mode", "Path color mode", PathRenderColorMode.RAINBOW, PathRenderColorMode.class));
    public final ColorSetting renderNodeColor = registerSetting(new ColorSetting(
        "render_node_color", "Node fill", 0x405A8CFF));
    public final ColorSetting renderGradientStartColor = registerSetting(new ColorSetting(
        "render_gradient_start_color", "Gradient start", 0xE65CC8FF));
    public final ColorSetting renderGradientEndColor = registerSetting(new ColorSetting(
        "render_gradient_end_color", "Gradient end", 0xE6FF4FD8));
    public final ColorSetting renderStartColor = registerSetting(new ColorSetting(
        "render_start_color", "Start node", 0xE600FFFF));
    public final ColorSetting renderEndColor = registerSetting(new ColorSetting(
        "render_end_color", "End node", 0xE6FF00FF));
    public final ColorSetting renderWalkColor = registerSetting(new ColorSetting(
        "render_walk_color", "Walk path", 0xC8F2F2F2));
    public final ColorSetting renderJumpColor = registerSetting(new ColorSetting(
        "render_jump_color", "Jump path", 0xE6FFD166));
    public final ColorSetting renderFallColor = registerSetting(new ColorSetting(
        "render_fall_color", "Fall path", 0xE6FF6B6B));
    public final ColorSetting renderWaterColor = registerSetting(new ColorSetting(
        "render_water_color", "Water path", 0xE65CC8FF));
    public final ColorSetting renderClimbColor = registerSetting(new ColorSetting(
        "render_climb_color", "Climb path", 0xE687D37C));
    public final ColorSetting renderFlyColor = registerSetting(new ColorSetting(
        "render_fly_color", "Fly path", 0xE6C792EA));
    public final DoubleSetting walkCost = registerSetting(new DoubleSetting("walk_cost", "Walk", 0.0, 0.0, 10.0));
    public final DoubleSetting diagonalCost = registerSetting(new DoubleSetting("diagonal_cost", "Diagonal", 0.0, 0.0, 10.0));
    public final DoubleSetting fullStepAscentBaseCost = registerSetting(new DoubleSetting(
        "full_step_ascent_base_cost", "Full step ascent base", 1.0, 0.0, 20.0));
    public final DoubleSetting fullStepAscentDyCost = registerSetting(new DoubleSetting(
        "full_step_ascent_dy_cost", "Full step ascent height", 0.5, 0.0, 10.0));
    public final DoubleSetting partialAscentCost = registerSetting(new DoubleSetting(
        "partial_ascent_cost", "Slab/stair ascent", 0.0, 0.0, 10.0));
    public final DoubleSetting jumpCost = registerSetting(new DoubleSetting("jump_cost", "Jump", 0.0, 0.0, 20.0));
    public final DoubleSetting parkourCost = registerSetting(new DoubleSetting("parkour_cost", "Parkour", 0.0, 0.0, 30.0));
    public final DoubleSetting fallDyCost = registerSetting(new DoubleSetting("fall_dy_cost", "Fall height", 0.1, 0.0, 10.0));
    public final DoubleSetting swimCost = registerSetting(new DoubleSetting("swim_cost", "Swim", 1.5, 0.0, 20.0));
    public final DoubleSetting climbCost = registerSetting(new DoubleSetting("climb_cost", "Climb", 2.0, 0.0, 20.0));
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
        "default_walk_max_iterations", "Default max iterations", 8000, 1000, 300000));
    public final IntSetting defaultWalkMaxLength = registerSetting(new IntSetting(
        "default_walk_max_length", "Default max length", 6000, 100, 50000));
    public final IntSetting instantWalkMaxIterations = registerSetting(new IntSetting(
        "instant_walk_max_iterations", "Instant max iterations", 2500, 1000, 100000));
    public final IntSetting instantWalkMaxLength = registerSetting(new IntSetting(
        "instant_walk_max_length", "Instant max length", 900, 100, 20000));
    public final IntSetting repairWalkMaxIterations = registerSetting(new IntSetting(
        "repair_walk_max_iterations", "Repair max iterations", 1500, 1000, 100000));
    public final IntSetting repairWalkMaxLength = registerSetting(new IntSetting(
        "repair_walk_max_length", "Repair max length", 400, 50, 10000));
    public final IntSetting queuedLongRangeMaxIterations = registerSetting(new IntSetting(
        "queued_long_range_max_iterations", "Queued segment iterations", 4500, 1000, 120000));
    public final IntSetting queuedLongRangeMaxLength = registerSetting(new IntSetting(
        "queued_long_range_max_length", "Queued segment length", 1200, 100, 30000));
    public final BooleanSetting earlyFallbackEnabled = registerSetting(new BooleanSetting(
        "early_fallback_enabled", "Early fallback", true));
    public final IntSetting earlyFallbackIterations = registerSetting(new IntSetting(
        "early_fallback_iterations", "Early fallback iterations", 700, 50, 50000));
    public final IntSetting earlyFallbackMinPathNodes = registerSetting(new IntSetting(
        "early_fallback_min_path_nodes", "Early fallback min nodes", 9, 2, 200));
    public final DoubleSetting earlyFallbackMinProgressRatio = registerSetting(new DoubleSetting(
        "early_fallback_min_progress_ratio", "Early fallback progress", 0.06, 0.0, 1.0));
    public final IntSetting instantCalculationTimeMs = registerSetting(new IntSetting(
        "instant_calculation_time_ms", "Instant time budget ms", 35, 0, 1000));
    public final IntSetting defaultCalculationTimeMs = registerSetting(new IntSetting(
        "default_calculation_time_ms", "Default time budget ms", 90, 0, 5000));
    public final IntSetting repairCalculationTimeMs = registerSetting(new IntSetting(
        "repair_calculation_time_ms", "Repair time budget ms", 25, 0, 1000));
    public final IntSetting queuedCalculationTimeMs = registerSetting(new IntSetting(
        "queued_calculation_time_ms", "Queued time budget ms", 60, 0, 5000));
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
    public final IntSetting parkourGroundedNoProgressReplanMs = registerSetting(new IntSetting(
        "parkour_grounded_no_progress_replan_ms", "Parkour grounded no progress ms", 1200, 100, 8000));
    public final IntSetting parkourPathReplanStaleMs = registerSetting(new IntSetting(
        "parkour_path_replan_stale_ms", "Parkour path stale repair ms", 1600, 100, 10000));
    public final IntSetting pathReplanHardStaleMs = registerSetting(new IntSetting(
        "path_replan_hard_stale_ms", "Hard stale replan ms", 1800, 100, 15000));
    public final IntSetting parkourPathReplanHardStaleMs = registerSetting(new IntSetting(
        "parkour_path_replan_hard_stale_ms", "Parkour hard stale replan ms", 4500, 100, 20000));
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
        "camera_rail_guide_lookahead_dist", "Camera rail lookahead", 2.65, 0.1, 16.0));
    public final DoubleSetting cameraNaturalFocusBlend = registerSetting(new DoubleSetting(
        "camera_natural_focus_blend", "Camera natural focus", 0.78, 0.25, 1.0));
    public final DoubleSetting cameraNaturalLateralOffset = registerSetting(new DoubleSetting(
        "camera_natural_lateral_offset", "Camera natural lateral", 0.26, 0.0, 1.5));
    public final DoubleSetting cameraNaturalVerticalOffset = registerSetting(new DoubleSetting(
        "camera_natural_vertical_offset", "Camera natural vertical", -0.12, -1.0, 1.0));
    public final DoubleSetting cameraMinForwardDot = registerSetting(new DoubleSetting(
        "camera_min_forward_dot", "Camera min forward dot", 0.05, -0.25, 0.75));
    public final DoubleSetting cameraHeightDeadband = registerSetting(new DoubleSetting(
        "camera_height_deadband", "Camera height deadband", 0.18, 0.0, 1.5));
    public final DoubleSetting cameraHeightStiffness = registerSetting(new DoubleSetting(
        "camera_height_stiffness", "Camera height stiffness", 0.16, 0.01, 1.0));
    public final DoubleSetting cameraHeightDamping = registerSetting(new DoubleSetting(
        "camera_height_damping", "Camera height damping", 0.72, 0.01, 0.99));
    public final DoubleSetting cameraHeightMaxStep = registerSetting(new DoubleSetting(
        "camera_height_max_step", "Camera height max step", 0.18, 0.01, 1.5));
    public final DoubleSetting cameraHeightUpLimit = registerSetting(new DoubleSetting(
        "camera_height_up_limit", "Camera height up limit", 5.0, 0.25, 24.0));
    public final DoubleSetting cameraHeightDownLimit = registerSetting(new DoubleSetting(
        "camera_height_down_limit", "Camera height down limit", 4.0, 0.25, 24.0));
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
    public final DoubleSetting parkourIdleYawDeadbandDeg = registerSetting(new DoubleSetting(
        "parkour_idle_yaw_deadband_deg", "Parkour idle yaw deadband", 1.0, 0.0, 30.0));
    public final DoubleSetting parkourIdlePitchDeadbandDeg = registerSetting(new DoubleSetting(
        "parkour_idle_pitch_deadband_deg", "Parkour idle pitch deadband", 2.0, 0.0, 30.0));
    public final DoubleSetting parkourActiveYawRetargetDeg = registerSetting(new DoubleSetting(
        "parkour_active_yaw_retarget_deg", "Parkour active yaw retarget", 3.0, 0.0, 45.0));
    public final DoubleSetting parkourActivePitchRetargetDeg = registerSetting(new DoubleSetting(
        "parkour_active_pitch_retarget_deg", "Parkour active pitch retarget", 3.0, 0.0, 45.0));
    public final IntSetting parkourRotationDurationMs = registerSetting(new IntSetting(
        "parkour_rotation_duration_ms", "Parkour rotation duration ms", 220, 50, 3000));
    public final DoubleSetting pitchMinHorizontalDistance = registerSetting(new DoubleSetting(
        "pitch_min_horizontal_distance", "Pitch min horizontal distance", 2.25, 0.0, 12.0));
    public final DoubleSetting pitchLandMaxAbsDeg = registerSetting(new DoubleSetting(
        "pitch_land_max_abs_deg", "Pitch land max abs", 22.0, 0.0, 89.0));
    public final DoubleSetting pitchWaterMaxAbsDeg = registerSetting(new DoubleSetting(
        "pitch_water_max_abs_deg", "Pitch water max abs", 8.0, 0.0, 89.0));
    public final DoubleSetting pitchSpringStiffness = registerSetting(new DoubleSetting(
        "pitch_spring_stiffness", "Pitch spring stiffness", 0.10, 0.01, 1.0));
    public final DoubleSetting pitchSpringDamping = registerSetting(new DoubleSetting(
        "pitch_spring_damping", "Pitch spring damping", 0.75, 0.01, 0.99));
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
        return INSTANCE.settings();
    }

    public static List<Setting<?>> generalSettings() {
        return List.of(INSTANCE.showDebug, INSTANCE.maxJumpHeight);
    }

    public static List<Setting<?>> movementCostSettings() {
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

    public static List<Setting<?>> renderingSettings() {
        return List.of(
            INSTANCE.renderPathNodes,
            INSTANCE.renderPathLines,
            INSTANCE.renderCameraRail,
            INSTANCE.renderMaxPathNodes,
            INSTANCE.renderMaxCameraNodes,
            INSTANCE.renderPathLineWidth,
            INSTANCE.renderCameraLineWidth,
            INSTANCE.renderPathColorMode,
            INSTANCE.renderNodeColor,
            INSTANCE.renderGradientStartColor,
            INSTANCE.renderGradientEndColor,
            INSTANCE.renderStartColor,
            INSTANCE.renderEndColor,
            INSTANCE.renderWalkColor,
            INSTANCE.renderJumpColor,
            INSTANCE.renderFallColor,
            INSTANCE.renderWaterColor,
            INSTANCE.renderClimbColor,
            INSTANCE.renderFlyColor);
    }

    public static List<Setting<?>> corridorCostSettings() {
        return List.of(
            INSTANCE.cardinalWallCost,
            INSTANCE.diagonalWallCost,
            INSTANCE.partialAscentEdgeCost,
            INSTANCE.partialAscentEntrySideCost,
            INSTANCE.openingEntryImbalanceCost);
    }

    public static List<Setting<?>> steeringSettings() {
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
        return List.of(
            INSTANCE.defaultWalkMaxIterations,
            INSTANCE.defaultWalkMaxLength,
            INSTANCE.instantWalkMaxIterations,
            INSTANCE.instantWalkMaxLength,
            INSTANCE.repairWalkMaxIterations,
            INSTANCE.repairWalkMaxLength,
            INSTANCE.queuedLongRangeMaxIterations,
            INSTANCE.queuedLongRangeMaxLength,
            INSTANCE.earlyFallbackEnabled,
            INSTANCE.earlyFallbackIterations,
            INSTANCE.earlyFallbackMinPathNodes,
            INSTANCE.earlyFallbackMinProgressRatio,
            INSTANCE.instantCalculationTimeMs,
            INSTANCE.defaultCalculationTimeMs,
            INSTANCE.repairCalculationTimeMs,
            INSTANCE.queuedCalculationTimeMs);
    }

    public static List<Setting<?>> executionSettings() {
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
        return List.of(
            INSTANCE.unstuckJumpMs,
            INSTANCE.unstuckBackupMs,
            INSTANCE.backupTicks,
            INSTANCE.pathReplanStaleMs,
            INSTANCE.pathReplanDriftDistance,
            INSTANCE.groundedNoProgressReplanMs,
            INSTANCE.parkourGroundedNoProgressReplanMs,
            INSTANCE.parkourPathReplanStaleMs,
            INSTANCE.pathReplanHardStaleMs,
            INSTANCE.parkourPathReplanHardStaleMs,
            INSTANCE.backupMaxHorizontalSpeed,
            INSTANCE.cornerAlignMinDistance,
            INSTANCE.cornerAlignMaxDistance,
            INSTANCE.cornerAlignMaxVertical,
            INSTANCE.cornerAlignMaxMs);
    }

    public static List<Setting<?>> longRangeSettings() {
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
        return List.of(
            INSTANCE.useCameraRail,
            INSTANCE.cameraRailReachedDist,
            INSTANCE.legacyCameraEyeY,
            INSTANCE.cameraRailGuideLookaheadDist,
            INSTANCE.cameraNaturalFocusBlend,
            INSTANCE.cameraNaturalLateralOffset,
            INSTANCE.cameraNaturalVerticalOffset,
            INSTANCE.cameraMinForwardDot,
            INSTANCE.cameraHeightDeadband,
            INSTANCE.cameraHeightStiffness,
            INSTANCE.cameraHeightDamping,
            INSTANCE.cameraHeightMaxStep,
            INSTANCE.cameraHeightUpLimit,
            INSTANCE.cameraHeightDownLimit,
            INSTANCE.rotationRedispatchCooldownMs,
            INSTANCE.idleYawDeadbandDeg,
            INSTANCE.idlePitchDeadbandDeg,
            INSTANCE.activeYawRetargetDeg,
            INSTANCE.activePitchRetargetDeg,
            INSTANCE.rotationDurationMs,
            INSTANCE.parkourIdleYawDeadbandDeg,
            INSTANCE.parkourIdlePitchDeadbandDeg,
            INSTANCE.parkourActiveYawRetargetDeg,
            INSTANCE.parkourActivePitchRetargetDeg,
            INSTANCE.parkourRotationDurationMs,
            INSTANCE.pitchMinHorizontalDistance,
            INSTANCE.pitchLandMaxAbsDeg,
            INSTANCE.pitchWaterMaxAbsDeg,
            INSTANCE.pitchSpringStiffness,
            INSTANCE.pitchSpringDamping,
            INSTANCE.cameraLookahead,
            INSTANCE.cameraMaxLateralDev);
    }

    public static List<Setting<?>> smoothingSettings() {
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

    public static void resetToDefaults() {
        INSTANCE.resetSettings();
    }

    public static void save() {
        
    }
}
