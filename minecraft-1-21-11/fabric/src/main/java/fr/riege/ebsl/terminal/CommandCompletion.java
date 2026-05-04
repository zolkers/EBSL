package fr.riege.ebsl.terminal;

import java.util.ArrayList;
import java.util.List;

public final class CommandCompletion {

    public static final CommandCompletion EMPTY = new CommandCompletion(List.of());

    private final List<List<String>> argChoices;

    private CommandCompletion(List<List<String>> argChoices) {
        this.argChoices = argChoices;
    }

    public List<String> suggest(int argIndex, String partial) {
        if (argIndex < 0 || argIndex >= argChoices.size()) return List.of();
        String lp = partial.toLowerCase();
        return argChoices.get(argIndex).stream()
            .filter(s -> s.toLowerCase().startsWith(lp))
            .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<List<String>> args = new ArrayList<>();

        public Builder arg(String... choices) {
            args.add(List.of(choices));
            return this;
        }

        public CommandCompletion build() {
            return new CommandCompletion(List.copyOf(args));
        }
    }
}
