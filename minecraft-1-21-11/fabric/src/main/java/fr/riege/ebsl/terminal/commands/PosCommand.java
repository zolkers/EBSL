package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "pos", description = "Show player position", scope = CommandScope.MC)
public final class PosCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        double x = ctx.mc().player.getX();
        double y = ctx.mc().player.getY();
        double z = ctx.mc().player.getZ();
        return CommandResult.ok(String.format("%.2f  %.2f  %.2f", x, y, z));
    }
}
