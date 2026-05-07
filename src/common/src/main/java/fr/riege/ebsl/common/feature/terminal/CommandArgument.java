package fr.riege.ebsl.common.feature.terminal;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public record CommandArgument(String name, Supplier<List<String>> suggestions) {
    public static CommandArgument named(String name) {
        return new CommandArgument(name, List::of);
    }

    public static CommandArgument choices(String name, String... choices) {
        List<String> values = List.of(choices);
        return new CommandArgument(name, () -> values);
    }

    public static CommandArgument dynamic(String name, Supplier<List<String>> suggestions) {
        return new CommandArgument(name, suggestions != null ? suggestions : List::of);
    }

    List<String> suggest(String partial) {
        String query = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return suggestions.get().stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(query))
            .toList();
    }

    String usageToken() {
        List<String> values = suggestions.get();
        if (!values.isEmpty() && values.size() <= 8) {
            return "<" + String.join("|", values) + ">";
        }
        return "<" + name + ">";
    }
}
