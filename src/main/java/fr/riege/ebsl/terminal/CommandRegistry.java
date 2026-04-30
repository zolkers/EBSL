package fr.riege.ebsl.terminal;

import fr.riege.ebsl.terminal.goal.GoalParameter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CommandRegistry {

    private record Entry(CommandMeta meta, CommandHandler handler) {}

    private static final Map<String, Entry> COMMANDS = new LinkedHashMap<>();

    private CommandRegistry() {}

    /** Register an annotated CommandHandler (uses @Command for metadata). */
    public static void register(CommandHandler handler) {
        Command ann = handler.getClass().getAnnotation(Command.class);
        if (ann == null) {
            throw new IllegalArgumentException(handler.getClass().getName() + " is missing @Command");
        }
        register(ann.name(), ann.description(), ann.usage(), ann.scope(), handler);
    }

    /** Register a CommandHandler with explicit metadata (no annotation required). */
    public static void register(String name, String description, String usage,
                                CommandScope scope, CommandHandler handler) {
        COMMANDS.put(name.toLowerCase(),
            new Entry(new CommandMeta(name.toLowerCase(), description, usage, scope), handler));
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

    public static List<CommandMeta> allMeta() {
        List<CommandMeta> out = new ArrayList<>();
        for (Entry e : COMMANDS.values()) out.add(e.meta());
        return Collections.unmodifiableList(out);
    }

    public static CommandHandler handler(String name) {
        Entry e = COMMANDS.get(name.toLowerCase());
        return e != null ? e.handler() : null;
    }

    public static List<CommandSuggestion> suggest(String input) {
        if (input.isBlank()) {
            return COMMANDS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> CommandSuggestion.of(e.getKey(), argsHint(e.getValue())))
                .toList();
        }

        int spaceIdx = input.indexOf(' ');

        // command-name phase: fuzzy filter
        if (spaceIdx < 0) {
            String query = input.toLowerCase();
            return COMMANDS.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), fuzzyScore(e.getKey(), query)))
                .filter(e -> e.getValue() >= 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Entry entry = COMMANDS.get(e.getKey());
                    return CommandSuggestion.of(e.getKey(), argsHint(entry));
                })
                .toList();
        }

        // arg phase
        String name = input.substring(0, spaceIdx).toLowerCase();
        Entry entry = COMMANDS.get(name);
        if (entry == null) return List.of();

        String afterName = input.substring(spaceIdx + 1);
        Map<String, CommandHandler> subs = entry.handler().subcommands();

        if (!subs.isEmpty()) {
            // subcommand phase: "goal <partial>" or "goal <sub> <args>"
            int subSpaceIdx = afterName.indexOf(' ');
            if (subSpaceIdx < 0) {
                // still typing subcommand name — fuzzy filter subcommand names
                String query = afterName.toLowerCase();
                return subs.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), fuzzyScore(e.getKey(), query)))
                    .filter(e -> query.isEmpty() || e.getValue() >= 0)
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .map(e -> {
                        CommandHandler sub = subs.get(e.getKey());
                        String hint = sub.params().stream()
                            .map(p -> "<" + p.label() + ">").collect(java.util.stream.Collectors.joining(" "));
                        return CommandSuggestion.of(e.getKey(), hint);
                    })
                    .toList();
            }
            // subcommand name typed, now in its arg phase
            String subName = afterName.substring(0, subSpaceIdx).toLowerCase();
            CommandHandler sub = subs.get(subName);
            if (sub == null) return List.of();
            return suggestParams(sub.params(), afterName.substring(subSpaceIdx + 1));
        }

        // direct params
        List<GoalParameter> params = entry.handler().params();
        if (!params.isEmpty()) {
            return suggestParams(params, afterName);
        }

        // static completer fallback
        String[] tokens = input.split(" ", -1);
        int argIndex = tokens.length - 2;
        String partial = tokens[tokens.length - 1];
        return entry.handler().completer().suggest(argIndex, partial).stream()
            .map(s -> CommandSuggestion.of(s, ""))
            .toList();
    }

    private static List<CommandSuggestion> suggestParams(List<GoalParameter> params, String argsStr) {
        if (params.isEmpty()) return List.of();
        Minecraft mc = Minecraft.getInstance();
        String[] parts = argsStr.split(" ", -1);
        int filled = parts.length - 1;
        if (filled >= params.size()) return List.of();
        StringBuilder fillSb = new StringBuilder();
        StringBuilder hintSb = new StringBuilder();
        for (int i = filled; i < params.size(); i++) {
            if (fillSb.length() > 0) { fillSb.append(' '); hintSb.append(' '); }
            fillSb.append(params.get(i).defaultValue(mc));
            hintSb.append('<').append(params.get(i).label()).append('>');
        }
        return fillSb.length() > 0
            ? List.of(CommandSuggestion.of(fillSb.toString(), hintSb.toString()))
            : List.of();
    }

    private static String argsHint(Entry entry) {
        String usage = entry.meta().usage();
        String name = entry.meta().name();
        String args = usage.startsWith(name) ? usage.substring(name.length()).trim() : usage;
        return args.isEmpty() ? entry.meta().description() : args;
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
