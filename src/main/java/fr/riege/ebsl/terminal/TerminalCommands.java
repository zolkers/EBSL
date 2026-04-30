package fr.riege.ebsl.terminal;

import fr.riege.ebsl.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.terminal.goal.GoalUiDefinition;
import fr.riege.ebsl.terminal.commands.DebugCommand;
import fr.riege.ebsl.terminal.commands.GoalTerminalCommand;
import fr.riege.ebsl.terminal.commands.HelpCommand;
import fr.riege.ebsl.terminal.commands.JumpHeightCommand;
import fr.riege.ebsl.terminal.commands.PosCommand;
import fr.riege.ebsl.terminal.commands.StatusCommand;
import fr.riege.ebsl.terminal.commands.StopCommand;
import fr.riege.ebsl.terminal.commands.TestCommand;
import fr.riege.ebsl.terminal.commands.TestXzCommand;
import fr.riege.ebsl.terminal.commands.UiCommand;

import java.util.stream.Collectors;

public final class TerminalCommands {

    private TerminalCommands() {}

    public static void bootstrap() {
        // terminal-only / utility
        CommandRegistry.register(new HelpCommand());
        CommandRegistry.register(new StopCommand());
        CommandRegistry.register(new PosCommand());
        CommandRegistry.register(new StatusCommand());
        CommandRegistry.register(new DebugCommand());
        CommandRegistry.register(new JumpHeightCommand());
        CommandRegistry.register(new UiCommand());

        // test / visualizer
        CommandRegistry.register(new TestCommand());
        CommandRegistry.register(new TestXzCommand());

        // navigation goals — all backed by GoalUiCatalog
        for (GoalUiDefinition def : GoalUiCatalog.all()) {
            String usage = def.id() + " " + def.parameters().stream()
                .map(p -> "<" + p.label() + ">")
                .collect(Collectors.joining(" "));
            CommandRegistry.register(
                def.id(), def.description(), usage.strip(), CommandScope.MC,
                new GoalTerminalCommand(def));
        }
    }
}
