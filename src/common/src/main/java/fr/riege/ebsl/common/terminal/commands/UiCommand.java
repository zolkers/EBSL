package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "ui", description = "Toggle the EBSL ImGui overlay", usage = "ui", scope = CommandScope.BOTH)
public final class UiCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean visible = EbslServices.ui().toggle();
        return CommandResult.ok("UI overlay: " + (visible ? "on" : "off"));
    }
}
