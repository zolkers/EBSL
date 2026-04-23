package fr.riege.ebsl.pathfinding.goal;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface GoalRequestStarter<T extends Goal> {
    void start(Minecraft mc, T goal, NavigationRequest request);
}
