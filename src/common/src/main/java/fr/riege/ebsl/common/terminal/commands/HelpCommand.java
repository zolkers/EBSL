package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandMeta;
import fr.riege.ebsl.common.terminal.CommandRegistry;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

import java.util.ArrayList;
import java.util.List;

@Command(name = "help", description = "List available commands", scope = CommandScope.TERMINAL)
public final class HelpCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        List<String> lines = new ArrayList<>();
        for (CommandMeta meta : CommandRegistry.allMeta()) {
            String args = meta.usage().startsWith(meta.name())
                ? meta.usage().substring(meta.name().length()).trim()
                : meta.usage();
            lines.add(meta.name() + (args.isEmpty() ? "" : " " + args) + " - " + meta.description());
        }
        return CommandResult.ok(lines);
    }
}
