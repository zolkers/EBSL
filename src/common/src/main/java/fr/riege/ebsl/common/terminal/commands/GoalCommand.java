package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.*;

import java.util.Locale;

@Command(name = "goal", description = "Run a navigation goal", usage = "goal <walk|column|test|testxz> [args]", scope = CommandScope.MC)
public final class GoalCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            return CommandResult.ok("Goals: walk, column, test, testxz");
        }

        String goal = ctx.arg(0).toLowerCase(Locale.ROOT);
        return switch (goal) {
            case "walk", "block" -> startBlock(ctx.shift(1), "goal " + goal + " <x> <y> <z>");
            case "column", "walkxz" -> startColumn(ctx.shift(1), "goal " + goal + " <x> <z>");
            case "test" -> startPathTest(ctx.shift(1), "goal test <x> <y> <z>");
            case "testxz" -> startPathTestXZ(ctx.shift(1), "goal testxz <x> <z>");
            default -> CommandResult.error("Unknown goal: " + goal + "  (type 'goal' for list)");
        };
    }

    @Override
    public CommandCompletion completer() {
        return CommandCompletion.builder()
            .arg("walk", "block", "column", "walkxz", "test", "testxz")
            .build();
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
