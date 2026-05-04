package fr.riege.ebsl.terminal;

import fr.riege.ebsl.terminal.commands.DebugCommand;
import fr.riege.ebsl.terminal.commands.GoalDispatchCommand;
import fr.riege.ebsl.terminal.commands.HelpCommand;
import fr.riege.ebsl.terminal.commands.JumpHeightCommand;
import fr.riege.ebsl.terminal.commands.PosCommand;
import fr.riege.ebsl.terminal.commands.StatusCommand;
import fr.riege.ebsl.terminal.commands.StopCommand;
import fr.riege.ebsl.terminal.commands.TestCommand;
import fr.riege.ebsl.terminal.commands.TestXzCommand;
import fr.riege.ebsl.terminal.commands.UiCommand;

public final class TerminalCommands {

    private TerminalCommands() {}

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
        CommandRegistry.register("goal", "Run a navigation goal", "goal <name> [args]",
            CommandScope.MC, new GoalDispatchCommand());
    }
}
