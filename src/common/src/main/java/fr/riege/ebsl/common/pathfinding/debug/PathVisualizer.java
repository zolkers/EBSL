package fr.riege.ebsl.common.pathfinding.debug;

public final class PathVisualizer {
    private static boolean enabled;

    private PathVisualizer() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void addExplored(int x, int y, int z) {
        // Rendering is provided by platform-specific layers later.
    }
}
