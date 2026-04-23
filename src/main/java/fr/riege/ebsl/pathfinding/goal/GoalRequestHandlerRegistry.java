package fr.riege.ebsl.pathfinding.goal;

import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class GoalRequestHandlerRegistry {
    private static final Map<Class<? extends Goal>, GoalRequestHandler<?>> HANDLERS = new LinkedHashMap<>();

    private GoalRequestHandlerRegistry() {
    }

    public static <T extends Goal> void register(GoalRequestHandler<T> handler) {
        GoalRequestHandler<?> previous = HANDLERS.putIfAbsent(handler.goalType(), handler);
        if (previous != null) {
            throw new IllegalStateException("Duplicate goal handler registration: " + handler.goalType().getSimpleName());
        }
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
        return Set.copyOf(HANDLERS.keySet());
    }

    private static <T extends Goal> void dispatch(GoalRequestHandler<T> handler, Minecraft mc, Goal goal, NavigationRequest request) {
        handler.start(mc, handler.goalType().cast(goal), request);
    }
}
