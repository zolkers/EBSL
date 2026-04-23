package fr.riege.ebsl.pathfinding.goal;

import net.minecraft.client.Minecraft;

public interface GoalRequestHandler<T extends Goal> {
    Class<T> goalType();

    void start(Minecraft mc, T goal, NavigationRequest request);
}
