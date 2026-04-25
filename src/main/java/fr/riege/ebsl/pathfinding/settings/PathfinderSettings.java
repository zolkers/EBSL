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
    }
}
