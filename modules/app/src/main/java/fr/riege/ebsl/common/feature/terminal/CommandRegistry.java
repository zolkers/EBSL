/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.util.*;

public final class CommandRegistry {
    private record Entry(CommandMeta meta, CommandHandler handler) {}

    private static final MapRegistry<String, Entry> COMMANDS = new MapRegistry<>(null);

    private CommandRegistry() {
    }

    public static void register(CommandHandler handler) {
        if (handler instanceof CommandSpec spec) {
            register(spec);
            return;
        }
        Command ann = handler.getClass().getAnnotation(Command.class);
        if (ann == null) {
            throw new IllegalArgumentException(handler.getClass().getName() + " is missing @Command");
        }
        register(ann.name(), ann.description(), ann.usage(), ann.scope(), handler);
    }

    public static void register(CommandSpec spec) {
        CommandMeta meta = spec.meta();
        register(meta.name(), meta.description(), meta.usage(), meta.scope(), spec);
    }

    public static void register(String name, String description, String usage,
                                CommandScope scope, CommandHandler handler) {
        COMMANDS.register(name.toLowerCase(),
            new Entry(new CommandMeta(name.toLowerCase(), description, usage, scope), handler));
    }

    public static CommandResult dispatch(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return CommandResult.ok();
        }
        String[] tokens = trimmed.split("\\s+");
        String name = tokens[0].toLowerCase();
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);

        Entry entry = COMMANDS.get(name);
        if (entry == null) {
            return CommandResult.error("Unknown command: " + name);
        }
        try {
            return entry.handler().execute(new CommandContext(args));
        } catch (NumberFormatException e) {
            return CommandResult.error("Invalid argument: " + e.getMessage());
        } catch (Exception e) {
            return CommandResult.error("Error: " + e.getMessage());
        }
    }

    public static List<CommandMeta> allMeta() {
        List<CommandMeta> out = new ArrayList<>();
        for (Entry e : COMMANDS.values()) {
            out.add(e.meta());
        }
        return Collections.unmodifiableList(out);
    }

    public static CommandHandler handler(String name) {
        Entry e = COMMANDS.get(name.toLowerCase());
        return e != null ? e.handler() : null;
    }

    public static List<CommandSuggestion> suggest(String input) {
        if (input.isBlank()) {
            return COMMANDS.keys().stream()
                .sorted()
                .map(key -> CommandSuggestion.of(key, argsHint(COMMANDS.get(key))))
                .toList();
        }
        int spaceIdx = input.indexOf(' ');
        if (spaceIdx < 0) {
            String query = input.toLowerCase();
            return COMMANDS.keys().stream()
                .map(key -> Map.entry(key, fuzzyScore(key, query)))
                .filter(e -> e.getValue() >= 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> CommandSuggestion.of(e.getKey(), argsHint(COMMANDS.get(e.getKey()))))
                .toList();
        }

        String name = input.substring(0, spaceIdx).toLowerCase();
        Entry entry = COMMANDS.get(name);
        if (entry == null) {
            return List.of();
        }
        String[] tokens = input.split(" ", -1);
        int argIndex = tokens.length - 2;
        String partial = tokens[tokens.length - 1];
        List<String> previousArgs = new ArrayList<>();
        Collections.addAll(previousArgs, Arrays.copyOfRange(tokens, 1, tokens.length - 1));
        return entry.handler().completer().suggest(previousArgs, argIndex, partial).stream()
            .map(s -> CommandSuggestion.of(s, ""))
            .toList();
    }

    private static String argsHint(Entry entry) {
        String usage = entry.meta().usage();
        String name = entry.meta().name();
        String args = usage.startsWith(name) ? usage.substring(name.length()).trim() : usage;
        return args.isEmpty() ? entry.meta().description() : args;
    }

    private static int fuzzyScore(String target, String query) {
        if (query.isEmpty()) return 0;
        int ti = 0;
        int qi = 0;
        int score = 0;
        int consec = 0;
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
