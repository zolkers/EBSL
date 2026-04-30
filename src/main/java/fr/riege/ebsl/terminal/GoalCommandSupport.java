package fr.riege.ebsl.terminal;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class GoalCommandSupport {

    private GoalCommandSupport() {}

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static int startNavigation(NavigationRequest request) {
        PathfindingManager.startGoal(minecraft(), request);
        return 1;
    }

    public static int currentBlockX() {
        return (int) Math.floor(minecraft().player.getX());
    }

    public static int currentBlockY() {
        return (int) Math.floor(minecraft().player.getY());
    }

    public static int currentBlockZ() {
        return (int) Math.floor(minecraft().player.getZ());
    }

    public static void sendClientMessage(String message) {
        Minecraft mc = minecraft();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }
}
