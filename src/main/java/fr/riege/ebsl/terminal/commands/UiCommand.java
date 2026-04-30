package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;

@Command(name = "ui", description = "Toggle the EBSL ImGui overlay", usage = "ui", scope = CommandScope.BOTH)
public final class UiCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean visible = EbslImGuiOverlay.toggle();
        return CommandResult.ok("UI overlay: " + (visible ? "on" : "off"));
    }
}
