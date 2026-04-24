package fr.riege.ebsl.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fr.riege.ebsl.command.goal.GoalUiDefinition;

public final class GoalRegistry {
    private static final Map<String, GoalCommandDefinition> COMMANDS = new LinkedHashMap<>();

    private GoalRegistry() {
    }

    public static void register(GoalCommandDefinition definition) {
        GoalCommandDefinition previous = COMMANDS.putIfAbsent(definition.id(), definition);
        if (previous != null) {
            throw new IllegalStateException("Duplicate goal command registration: " + definition.id());
        }
    }

    public static Collection<GoalCommandDefinition> commands() {
        return List.copyOf(COMMANDS.values());
    }

    public static List<GoalUiDefinition> uiDefinitions() {
        return COMMANDS.values().stream()
            .map(GoalCommandDefinition::uiDefinition)
            .flatMap(Optional::stream)
            .toList();
    }

    public static boolean isEmpty() {
        return COMMANDS.isEmpty();
    }

    public static Set<String> commandIds() {
        return Set.copyOf(COMMANDS.keySet());
    }
}
