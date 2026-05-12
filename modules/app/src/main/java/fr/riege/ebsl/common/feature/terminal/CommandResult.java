package fr.riege.ebsl.common.feature.terminal;

import java.util.Arrays;
import java.util.List;

public record CommandResult(boolean success, List<String> lines) {

    public static CommandResult ok(String... lines) {
        return new CommandResult(true, Arrays.asList(lines));
    }

    public static CommandResult ok(List<String> lines) {
        return new CommandResult(true, lines);
    }

    public static CommandResult error(String message) {
        return new CommandResult(false, List.of(message));
    }

    public static CommandResult noPlayer() {
        return error("Not connected to a server.");
    }

    public static CommandResult badUsage(String usage) {
        return error("Usage: " + usage);
    }
}
