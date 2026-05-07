package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.terminal.CommandContext;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import java.util.Locale;

final class EbslCommandActionRegistry {
    private static final MapRegistry<String, EbslCommandActionHandler> HANDLERS = new MapRegistry<>(null);

    static {
        register(EbslCommandAction.RUN, EbslCommand::runFile);
        register(EbslCommandAction.INLINE, EbslCommand::runInline);
        register(EbslCommandAction.STOP, context -> {
            EbslScriptTask.INSTANCE.setEnabled(false);
            EbslScriptTask.INSTANCE.onDisable();
            return CommandResult.ok("EBSL stopped.");
        });
        register(EbslCommandAction.STATUS, context -> CommandResult.ok("EBSL: " + EbslScriptTask.INSTANCE.status()));
        register(EbslCommandAction.TASKS, context -> CommandResult.ok(EbslCommand.taskLines()));
    }

    private EbslCommandActionRegistry() {
    }

    static CommandResult execute(String action, CommandContext context) {
        EbslCommandActionHandler handler = HANDLERS.get(normalize(action));
        return handler == null ? CommandResult.badUsage(EbslCommand.usage()) : handler.execute(context);
    }

    private static void register(EbslCommandAction action, EbslCommandActionHandler handler) {
        HANDLERS.register(normalize(action.id()), handler);
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
