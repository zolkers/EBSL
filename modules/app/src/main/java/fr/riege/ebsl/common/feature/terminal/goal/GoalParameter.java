package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public record GoalParameter(String id, String label, DefaultProvider defaultProvider) {
    public int defaultValue(IPlayerLayer player) {
        return defaultProvider.value(player);
    }

    @FunctionalInterface
    public interface DefaultProvider {
        int value(IPlayerLayer player);
    }

    public static GoalParameter constant(String id, String label, int value) {
        return new GoalParameter(id, label, player -> value);
    }

    public static GoalParameter currentX() {
        return new GoalParameter("x", "X", p -> (int) Math.floor(p.position().x()));
    }

    public static GoalParameter currentY() {
        return new GoalParameter("y", "Y", p -> (int) Math.floor(p.position().y()));
    }

    public static GoalParameter currentZ() {
        return new GoalParameter("z", "Z", p -> (int) Math.floor(p.position().z()));
    }
}
