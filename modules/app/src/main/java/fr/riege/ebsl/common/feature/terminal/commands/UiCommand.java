package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;

@Command(name = CommandIds.UI, description = "Toggle the EBSL ImGui overlay", usage = CommandIds.UI, scope = CommandScope.BOTH)
public final class UiCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean visible = EbslServices.ui().toggle();
        return CommandResult.ok("UI overlay: " + (visible ? "on" : "off"));
    }
}
