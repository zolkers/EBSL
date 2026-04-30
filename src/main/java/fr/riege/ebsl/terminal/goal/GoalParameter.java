package fr.riege.ebsl.terminal.goal;

import net.minecraft.client.Minecraft;

public record GoalParameter(String id, String label, DefaultProvider defaultProvider) {
    public int defaultValue(Minecraft minecraft) {
        return defaultProvider.value(minecraft);
    }

    public interface DefaultProvider {
        int value(Minecraft minecraft);
    }

    public static GoalParameter constant(String id, String label, int value) {
        return new GoalParameter(id, label, minecraft -> value);
    }

    public static GoalParameter currentX() {
        return new GoalParameter("x", "X", minecraft -> minecraft.player != null ? (int) Math.floor(minecraft.player.getX()) : 0);
    }

    public static GoalParameter currentY() {
        return new GoalParameter("y", "Y", minecraft -> minecraft.player != null ? (int) Math.floor(minecraft.player.getY()) : 0);
    }

    public static GoalParameter currentZ() {
        return new GoalParameter("z", "Z", minecraft -> minecraft.player != null ? (int) Math.floor(minecraft.player.getZ()) : 0);
    }
}
