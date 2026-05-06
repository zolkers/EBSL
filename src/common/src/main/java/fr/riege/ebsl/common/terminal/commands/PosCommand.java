package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "pos", description = "Show player position", scope = CommandScope.MC)
public final class PosCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        Vec3d pos = EbslServices.platform().player().position();
        return CommandResult.ok(String.format("%.2f  %.2f  %.2f", pos.x(), pos.y(), pos.z()));
    }
}
