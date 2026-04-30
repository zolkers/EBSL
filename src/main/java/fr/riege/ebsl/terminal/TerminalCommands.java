package fr.riege.ebsl.terminal;

import fr.riege.ebsl.terminal.commands.AxisXCommand;
import fr.riege.ebsl.terminal.commands.AxisZCommand;
import fr.riege.ebsl.terminal.commands.ChunkCommand;
import fr.riege.ebsl.terminal.commands.ColumnCommand;
import fr.riege.ebsl.terminal.commands.DebugCommand;
import fr.riege.ebsl.terminal.commands.FlyCommand;
import fr.riege.ebsl.terminal.commands.GetToCommand;
import fr.riege.ebsl.terminal.commands.HelpCommand;
import fr.riege.ebsl.terminal.commands.JumpHeightCommand;
import fr.riege.ebsl.terminal.commands.NearCommand;
import fr.riege.ebsl.terminal.commands.NoReplanCommand;
import fr.riege.ebsl.terminal.commands.OffsetCommand;
import fr.riege.ebsl.terminal.commands.PosCommand;
import fr.riege.ebsl.terminal.commands.PreciseCommand;
import fr.riege.ebsl.terminal.commands.RectCommand;
import fr.riege.ebsl.terminal.commands.StatusCommand;
import fr.riege.ebsl.terminal.commands.StopCommand;
import fr.riege.ebsl.terminal.commands.TestCommand;
import fr.riege.ebsl.terminal.commands.TestXzCommand;
import fr.riege.ebsl.terminal.commands.UiCommand;
import fr.riege.ebsl.terminal.commands.WalkCommand;
import fr.riege.ebsl.terminal.commands.WalkXzCommand;
import fr.riege.ebsl.terminal.commands.YCommand;

public final class TerminalCommands {

    private TerminalCommands() {}

    public static void bootstrap() {
        // meta / terminal-only
        CommandRegistry.register(new HelpCommand());
        CommandRegistry.register(new StopCommand());
        CommandRegistry.register(new PosCommand());
        CommandRegistry.register(new StatusCommand());
        CommandRegistry.register(new DebugCommand());
        CommandRegistry.register(new JumpHeightCommand());
        CommandRegistry.register(new UiCommand());

        // navigation goals
        CommandRegistry.register(new WalkCommand());
        CommandRegistry.register(new FlyCommand());
        CommandRegistry.register(new WalkXzCommand());
        CommandRegistry.register(new NearCommand());
        CommandRegistry.register(new GetToCommand());
        CommandRegistry.register(new YCommand());
        CommandRegistry.register(new OffsetCommand());
        CommandRegistry.register(new PreciseCommand());
        CommandRegistry.register(new NoReplanCommand());

        // area goals
        CommandRegistry.register(new AxisXCommand());
        CommandRegistry.register(new AxisZCommand());
        CommandRegistry.register(new ColumnCommand());
        CommandRegistry.register(new RectCommand());
        CommandRegistry.register(new ChunkCommand());

        // test / visualizer
        CommandRegistry.register(new TestCommand());
        CommandRegistry.register(new TestXzCommand());
    }
}
