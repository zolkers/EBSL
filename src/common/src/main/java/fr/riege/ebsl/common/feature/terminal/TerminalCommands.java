package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.commands.*;

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
        CommandRegistry.register(GoalCommand.spec());
    }
}
