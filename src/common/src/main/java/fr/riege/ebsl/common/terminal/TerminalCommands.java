package fr.riege.ebsl.common.terminal;

import fr.riege.ebsl.common.terminal.commands.DebugCommand;
import fr.riege.ebsl.common.terminal.commands.GoalCommand;
import fr.riege.ebsl.common.terminal.commands.HelpCommand;
import fr.riege.ebsl.common.terminal.commands.JumpHeightCommand;
import fr.riege.ebsl.common.terminal.commands.PosCommand;
import fr.riege.ebsl.common.terminal.commands.StatusCommand;
import fr.riege.ebsl.common.terminal.commands.StopCommand;
import fr.riege.ebsl.common.terminal.commands.TestCommand;
import fr.riege.ebsl.common.terminal.commands.TestXzCommand;
import fr.riege.ebsl.common.terminal.commands.UiCommand;

public final class TerminalCommands {
    private TerminalCommands() {
    }

    public static void bootstrap() {
        CommandRegistry.register(new HelpCommand());
        CommandRegistry.register(new StopCommand());
        CommandRegistry.register(new PosCommand());
        CommandRegistry.register(new StatusCommand());
        CommandRegistry.register(new DebugCommand());
        CommandRegistry.register(new JumpHeightCommand());
        CommandRegistry.register(new UiCommand());
        CommandRegistry.register(new TestCommand());
        CommandRegistry.register(new TestXzCommand());
        CommandRegistry.register(new GoalCommand());
    }
}
