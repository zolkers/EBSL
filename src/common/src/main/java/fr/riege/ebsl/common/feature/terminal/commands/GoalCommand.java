package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GoalCommand {
    private static final String WALK = "walk";
    private static final String BLOCK = "block";
    private static final String COLUMN = "column";
    private static final String WALK_XZ = "walkxz";
    private static final String TEST = "test";
    private static final String TEST_XZ = "testxz";
    private static final List<String> LEGACY_GOAL_IDS = List.of(BLOCK, TEST, TEST_XZ);
    private static final List<String> GOAL_IDS = goalIds();
    private static final String GOAL_LIST = String.join(", ", GOAL_IDS);

    private GoalCommand() {
    }

    public static CommandSpec spec() {
        return CommandSpec.named(CommandIds.GOAL)
            .description("Run a navigation goal")
            .usage(CommandIds.GOAL + " <goal> [args]")
            .mcOnly()
            .argument(CommandArgument.dynamic("goal", () -> GOAL_IDS))
            .completion(goalCompletion())
            .executes(GoalCommand::execute)
            .build();
    }

    private static CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            return CommandResult.ok("Goals: " + GOAL_LIST);
        }

        String goal = ctx.arg(0).toLowerCase(Locale.ROOT);
        return switch (goal) {
            case BLOCK -> startCatalogGoal(WALK, ctx.shift(1));
            case COLUMN -> startColumnGoal(ctx.shift(1));
            case TEST -> startPathTest(ctx.shift(1), CommandIds.GOAL + " " + TEST + " <x> <y> <z>");
            case TEST_XZ -> startPathTestXZ(ctx.shift(1), CommandIds.GOAL + " " + TEST_XZ + " <x> <z>");
            default -> startCatalogGoal(goal, ctx.shift(1));
        };
    }

    private static CommandResult startCatalogGoal(String goal, CommandContext ctx) {
        GoalUiDefinition definition;
        try {
            definition = GoalUiCatalog.byId(goal);
        } catch (IllegalArgumentException exception) {
            return CommandResult.error("Unknown goal: " + goal + "  (type '" + CommandIds.GOAL + "' for list)");
        }
        if (ctx.argCount() != definition.parameters().size()) {
            return CommandResult.badUsage(usage(definition));
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i < definition.parameters().size(); i++) {
            values.put(definition.parameters().get(i).id(), ctx.argInt(i));
        }
        definition.execute(EbslServices.navigation(), values);
        return CommandResult.ok("Goal " + definition.id() + " " + String.join(" ", ctx.args()));
    }

    private static CommandResult startColumnGoal(CommandContext ctx) {
        if (ctx.argCount() == 3) {
            return startCatalogGoal(COLUMN, ctx);
        }
        if (ctx.argCount() != 2) {
            return CommandResult.badUsage(CommandIds.GOAL + " " + COLUMN + " <x> <z> [radius]");
        }
        int x = ctx.argInt(0);
        int z = ctx.argInt(1);
        EbslServices.navigation().startColumnGoal(x, z);
        return CommandResult.ok("Goal column to " + x + " " + z);
    }

    private static CommandResult startPathTest(CommandContext ctx, String usage) {
        if (ctx.argCount() != 3) {
            return CommandResult.badUsage(usage);
        }
        int x = ctx.argInt(0);
        int y = ctx.argInt(1);
        int z = ctx.argInt(2);
        EbslServices.navigation().startPathTest(x, y, z);
        return CommandResult.ok("Path test to " + x + " " + y + " " + z);
    }

    private static CommandResult startPathTestXZ(CommandContext ctx, String usage) {
        if (ctx.argCount() != 2) {
            return CommandResult.badUsage(usage);
        }
        int x = ctx.argInt(0);
        int z = ctx.argInt(1);
        EbslServices.navigation().startPathTestXZ(x, z);
        return CommandResult.ok("Path test XZ to " + x + " " + z);
    }

    private static CommandCompletion goalCompletion() {
        CommandCompletion.Builder builder = CommandCompletion.builder();
        int maxArgs = maxGoalParameterCount() + 1;
        for (int i = 0; i < maxArgs; i++) {
            builder.dynamic(GoalCommand::suggestGoalToken);
        }
        return builder.build();
    }

    private static List<String> suggestGoalToken(CommandCompletion.Context context) {
        if (context.argIndex() == 0) {
            return filter(GOAL_IDS, context.partial());
        }

        GoalUiDefinition definition = definitionForInput(context.previousArg(0));
        if (definition == null) {
            return List.of();
        }
        int paramIndex = context.argIndex() - 1;
        if (paramIndex >= definition.parameters().size()) {
            return List.of();
        }
        GoalParameter parameter = definition.parameters().get(paramIndex);
        return filter(List.of(defaultSuggestion(parameter)), context.partial());
    }

    private static GoalUiDefinition definitionForInput(String goal) {
        String normalized = goal == null ? "" : goal.toLowerCase(Locale.ROOT);
        if (BLOCK.equals(normalized)) {
            normalized = WALK;
        }
        try {
            return GoalUiCatalog.byId(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String defaultSuggestion(GoalParameter parameter) {
        try {
            return Integer.toString(parameter.defaultValue(EbslServices.platform().player()));
        } catch (Exception exception) {
            try {
                return Integer.toString(parameter.defaultValue(null));
            } catch (Exception ignored) {
                return "0";
            }
        }
    }

    private static String usage(GoalUiDefinition definition) {
        List<String> parts = new ArrayList<>();
        parts.add(CommandIds.GOAL);
        parts.add(definition.id());
        for (GoalParameter parameter : definition.parameters()) {
            parts.add("<" + parameter.id() + ">");
        }
        return String.join(" ", parts);
    }

    private static int maxGoalParameterCount() {
        return GoalUiCatalog.all().stream()
            .mapToInt(def -> def.parameters().size())
            .max()
            .orElse(0);
    }

    private static List<String> goalIds() {
        List<String> ids = new ArrayList<>();
        for (GoalUiDefinition definition : GoalUiCatalog.all()) {
            ids.add(definition.id());
        }
        for (String legacy : LEGACY_GOAL_IDS) {
            if (!ids.contains(legacy)) {
                ids.add(legacy);
            }
        }
        return List.copyOf(ids);
    }

    private static List<String> filter(List<String> values, String partial) {
        String query = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(query))
            .toList();
    }
}
