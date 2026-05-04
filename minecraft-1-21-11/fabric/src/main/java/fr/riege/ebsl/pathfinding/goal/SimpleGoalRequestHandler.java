package fr.riege.ebsl.pathfinding.goal;

import net.minecraft.client.Minecraft;

public record SimpleGoalRequestHandler<T extends Goal>(
    Class<T> goalType,
    GoalRequestStarter<T> starter
) implements GoalRequestHandler<T> {
    @Override
    public void start(Minecraft mc, T goal, NavigationRequest request) {
        starter.start(mc, goal, request);
    }
}
