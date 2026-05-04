package fr.riege.ebsl.pathfinding.goal;

import fr.riege.ebsl.registry.MapRegistry;
import net.minecraft.client.Minecraft;

import java.util.Set;

public final class GoalRequestHandlerRegistry {
    private static final MapRegistry<Class<? extends Goal>, GoalRequestHandler<?>> HANDLERS = new MapRegistry<>(null);

    private GoalRequestHandlerRegistry() {
    }

    public static <T extends Goal> void register(GoalRequestHandler<T> handler) {
        HANDLERS.register(handler.goalType(), handler);
    }

    public static void start(Minecraft mc, NavigationRequest request) {
        Goal goal = request.goal();
        GoalRequestHandler<?> exact = HANDLERS.get(goal.getClass());
        if (exact != null) {
            dispatch(exact, mc, goal, request);
            return;
        }

        for (GoalRequestHandler<?> handler : HANDLERS.values()) {
            if (handler.goalType().isInstance(goal)) {
                dispatch(handler, mc, goal, request);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported goal type: " + goal.debugName());
    }

    public static boolean isEmpty() {
        return HANDLERS.isEmpty();
    }

    public static Set<Class<? extends Goal>> registeredGoalTypes() {
        return HANDLERS.keys();
    }

    private static <T extends Goal> void dispatch(GoalRequestHandler<T> handler, Minecraft mc, Goal goal, NavigationRequest request) {
        handler.start(mc, handler.goalType().cast(goal), request);
    }
}
