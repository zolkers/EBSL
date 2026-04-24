package fr.riege.ebsl.pathfinding.settings;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.settings.Settingable;

import java.util.List;

public final class PathfinderSettings extends Settingable {
    private static final PathfinderSettings INSTANCE = new PathfinderSettings();
    public final BooleanSetting showDebug = registerSetting(new BooleanSetting("show_debug", "Show debug", true));
    public final IntSetting maxJumpHeight = registerSetting(new IntSetting("max_jump_height", "Max jump height", 1, 1, 5));

    private PathfinderSettings() {
    }

    public static PathfinderSettings instance() {
        return INSTANCE;
    }

    public static List<Setting<?>> all() {
        syncFromConfig();
        return INSTANCE.settings();
    }

    public static void apply() {
        PathfinderConfig.SHOW_DEBUG.set(INSTANCE.showDebug.value());
        PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.set(INSTANCE.maxJumpHeight.value());
    }

    public static void resetToDefaults() {
        INSTANCE.resetSettings();
        apply();
    }

    private static void syncFromConfig() {
        INSTANCE.showDebug.setValue(PathfinderConfig.SHOW_DEBUG.get());
        INSTANCE.maxJumpHeight.setValue(PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get());
    }
}
