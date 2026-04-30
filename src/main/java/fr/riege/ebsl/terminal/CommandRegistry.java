package fr.riege.ebsl.terminal;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CommandRegistry {

    private record Entry(Command meta, CommandHandler handler) {}

    private static final Map<String, Entry> COMMANDS = new LinkedHashMap<>();

    private CommandRegistry() {}

    public static void register(CommandHandler handler) {
        Command meta = handler.getClass().getAnnotation(Command.class);
        if (meta == null) {
            throw new IllegalArgumentException(handler.getClass().getName() + " is missing @Command");
        }
        COMMANDS.put(meta.name().toLowerCase(), new Entry(meta, handler));
    }

    public static void dispatch(String input, Minecraft mc) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return;

        TerminalLog.addInput("> " + trimmed);

        String[] tokens = trimmed.split("\\s+");
        String name = tokens[0].toLowerCase();
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);

        Entry entry = COMMANDS.get(name);
        if (entry == null) {
            TerminalLog.addError("Unknown command: " + name + "  (type 'help' for a list)");
            return;
        }

        CommandScope scope = entry.meta().scope();
        if (scope == CommandScope.MC && mc.player == null) {
            TerminalLog.addError("'" + name + "' requires an active player session.");
            return;
        }

        CommandResult result;
        try {
            result = entry.handler().execute(new CommandContext(mc, args));
        } catch (NumberFormatException e) {
            TerminalLog.addError("Invalid argument: " + e.getMessage());
            return;
        } catch (Exception e) {
            TerminalLog.addError("Error: " + e.getMessage());
            return;
        }

        for (String line : result.lines()) {
            if (result.success()) {
                TerminalLog.addOutput(line);
            } else {
                TerminalLog.addError(line);
            }
        }
    }

    public static String usageFor(String name) {
        Entry entry = COMMANDS.get(name.toLowerCase());
        return entry == null ? "" : entry.meta().usage();
    }

    public static List<Command> allMeta() {
        List<Command> out = new ArrayList<>();
        for (Entry e : COMMANDS.values()) out.add(e.meta());
        return Collections.unmodifiableList(out);
    }

    public static List<String> suggest(String input) {
        if (input.isBlank()) {
            return COMMANDS.keySet().stream().sorted().toList();
        }
        int spaceIdx = input.indexOf(' ');
        if (spaceIdx < 0) {
            String query = input.toLowerCase();
            return COMMANDS.keySet().stream()
                .map(k -> Map.entry(k, fuzzyScore(k, query)))
                .filter(e -> e.getValue() >= 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
        }
        String name = input.substring(0, spaceIdx).toLowerCase();
        Entry entry = COMMANDS.get(name);
        if (entry == null) return List.of();
        String[] tokens = input.split(" ", -1);
        int argIndex = tokens.length - 2;
        String partial = tokens[tokens.length - 1];
        return entry.handler().completer().suggest(argIndex, partial);
    }

    private static int fuzzyScore(String target, String query) {
        if (query.isEmpty()) return 0;
        int ti = 0, qi = 0, score = 0, consec = 0;
        while (ti < target.length() && qi < query.length()) {
            if (target.charAt(ti) == query.charAt(qi)) {
                consec++;
                score += consec * consec;
                if (ti == 0) score += 4;
                qi++;
            } else {
                consec = 0;
            }
            ti++;
        }
        return qi == query.length() ? score : -1;
    }
}
