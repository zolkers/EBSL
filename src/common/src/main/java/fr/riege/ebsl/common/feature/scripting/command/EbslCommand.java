package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.terminal.CommandArgument;
import fr.riege.ebsl.common.feature.terminal.CommandCompletion;
import fr.riege.ebsl.common.feature.terminal.CommandContext;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import fr.riege.ebsl.common.feature.terminal.CommandSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EbslCommand {
    private EbslCommand() {
    }

    public static CommandSpec spec() {
        return CommandSpec.named("ebsl")
            .description("Run and inspect EBSL scripts")
            .usage(usage())
            .bothScopes()
            .argument(CommandArgument.dynamic("action", EbslCommandAction::ids))
            .completion(CommandCompletion.builder()
                .dynamic(EbslCommand::suggest)
                .dynamic(EbslCommand::suggest)
                .build())
            .executes(EbslCommand::execute)
            .build();
    }

    private static CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            return CommandResult.ok("Usage: " + usage());
        }
        return EbslCommandActionRegistry.execute(ctx.arg(0), ctx);
    }

    static String usage() {
        return "ebsl <run|inline|stop|status|tasks> [args]";
    }

    static CommandResult runFile(CommandContext ctx) {
        if (ctx.argCount() != 2) {
            return CommandResult.badUsage("ebsl run <file.ebsl>");
        }
        EbslScriptTask.INSTANCE.runFile(ctx.arg(1));
        return CommandResult.ok("Running EBSL file " + ctx.arg(1));
    }

    static CommandResult runInline(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            return CommandResult.badUsage("ebsl inline <script tokens>");
        }
        List<String> parts = new ArrayList<>();
        for (int i = 1; i < ctx.argCount(); i++) {
            parts.add(ctx.arg(i));
        }
        EbslScriptTask.INSTANCE.runInline(String.join(" ", parts));
        return CommandResult.ok("Running inline EBSL.");
    }

    static List<String> taskLines() {
        List<String> lines = new ArrayList<>();
        lines.add("EBSL task catalogue: " + EbslNodeType.values().length);
        EbslNodeCategory category = null;
        for (EbslNodeType type : EbslNodeType.values()) {
            if (category != type.category()) {
                category = type.category();
                lines.add("[" + category.id() + "]");
            }
            lines.add((type.executable() ? "  " : "  *") + type.id());
        }
        lines.add("* = declared for compatibility, runtime backend not implemented yet");
        lines.add("Executable script nodes: " + EbslNodeRegistry.canonicalNodes().size());
        for (EbslNode node : EbslNodeRegistry.canonicalNodes()) {
            lines.add("  " + node.id());
        }
        return lines;
    }

    private static List<String> suggest(CommandCompletion.Context context) {
        if (context.argIndex() == 0) {
            return filter(EbslCommandAction.ids(), context.partial());
        }
        if ("tasks".equalsIgnoreCase(context.previousArg(0))) {
            return filter(EbslNodeType.ids(), context.partial());
        }
        return List.of();
    }

    private static List<String> filter(List<String> values, String partial) {
        String query = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(query))
            .toList();
    }
}
