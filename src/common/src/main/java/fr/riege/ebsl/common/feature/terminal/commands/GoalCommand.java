package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;

import java.util.Locale;

public final class GoalCommand {
    private static final String WALK = "walk";
    private static final String BLOCK = "block";
    private static final String COLUMN = "column";
    private static final String WALK_XZ = "walkxz";
    private static final String TEST = "test";
    private static final String TEST_XZ = "testxz";
    private static final String GOAL_LIST = String.join(", ", WALK, COLUMN, TEST, TEST_XZ);

    private GoalCommand() {
    }

    public static CommandSpec spec() {
        return CommandSpec.named(CommandIds.GOAL)
            .description("Run a navigation goal")
            .usage(CommandIds.GOAL + " <walk|column|test|testxz> [args]")
            .mcOnly()
            .choices("goal", WALK, BLOCK, COLUMN, WALK_XZ, TEST, TEST_XZ)
            .executes(GoalCommand::execute)
            .build();
    }

    private static CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            return CommandResult.ok("Goals: " + GOAL_LIST);
        }

        String goal = ctx.arg(0).toLowerCase(Locale.ROOT);
        return switch (goal) {
            case WALK, BLOCK -> startBlock(ctx.shift(1), CommandIds.GOAL + " " + goal + " <x> <y> <z>");
            case COLUMN, WALK_XZ -> startColumn(ctx.shift(1), CommandIds.GOAL + " " + goal + " <x> <z>");
            case TEST -> startPathTest(ctx.shift(1), CommandIds.GOAL + " " + TEST + " <x> <y> <z>");
            case TEST_XZ -> startPathTestXZ(ctx.shift(1), CommandIds.GOAL + " " + TEST_XZ + " <x> <z>");
            default -> CommandResult.error("Unknown goal: " + goal + "  (type '" + CommandIds.GOAL + "' for list)");
        };
    }

    private static CommandResult startBlock(CommandContext ctx, String usage) {
        if (ctx.argCount() != 3) {
            return CommandResult.badUsage(usage);
        }
        int x = ctx.argInt(0);
        int y = ctx.argInt(1);
        int z = ctx.argInt(2);
        EbslServices.navigation().startBlockGoal(x, y, z);
        return CommandResult.ok("Goal walk to " + x + " " + y + " " + z);
    }

    private static CommandResult startColumn(CommandContext ctx, String usage) {
        if (ctx.argCount() != 2) {
            return CommandResult.badUsage(usage);
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
}
