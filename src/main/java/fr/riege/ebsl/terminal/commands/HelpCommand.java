package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandRegistry;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

import java.util.ArrayList;
import java.util.List;

@Command(name = "help", description = "List available commands", scope = CommandScope.TERMINAL)
public final class HelpCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        List<String> lines = new ArrayList<>();
        for (Command meta : CommandRegistry.allMeta()) {
            String scope = "[" + meta.scope().name().toLowerCase() + "]";
            String line = String.format("%-16s %-10s %s", meta.name(),
                meta.usage().isEmpty() ? "" : meta.usage().substring(meta.name().length()).trim(),
                scope + (meta.description().isEmpty() ? "" : "  " + meta.description()));
            lines.add(line);
        }
        return CommandResult.ok(lines);
    }
}
