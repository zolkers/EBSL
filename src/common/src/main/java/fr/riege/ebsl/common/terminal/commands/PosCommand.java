package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.*;

@Command(name = "pos", description = "Show player position", scope = CommandScope.MC)
public final class PosCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        Vec3d pos = EbslServices.platform().player().position();
        return CommandResult.ok(String.format("%.2f  %.2f  %.2f", pos.x(), pos.y(), pos.z()));
    }
}
