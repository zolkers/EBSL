package fr.riege.ebsl.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fr.riege.ebsl.command.goal.GoalUiDefinition;
import fr.riege.ebsl.registry.MapRegistry;

public final class GoalRegistry {
    private static final MapRegistry<String, GoalCommandDefinition> COMMANDS = new MapRegistry<>(null);

    private GoalRegistry() {
    }

    public static void register(GoalCommandDefinition definition) {
        COMMANDS.register(definition.id(), definition);
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
        return COMMANDS.keys();
    }
}
