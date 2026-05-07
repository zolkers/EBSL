package fr.riege.ebsl.common.feature.terminal;

import java.util.ArrayList;
import java.util.List;

public final class CommandCompletion {

    public static final CommandCompletion EMPTY = new CommandCompletion(List.of());

    private final List<SuggestionSource> argChoices;

    private CommandCompletion(List<SuggestionSource> argChoices) {
        this.argChoices = argChoices;
    }

    public List<String> suggest(int argIndex, String partial) {
        if (argIndex < 0 || argIndex >= argChoices.size()) return List.of();
        return argChoices.get(argIndex).suggest(partial);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CommandCompletion fromArguments(List<CommandArgument> arguments) {
        Builder builder = builder();
        if (arguments != null) {
            for (CommandArgument argument : arguments) {
                builder.argument(argument);
            }
        }
        return builder.build();
    }

    public static final class Builder {
        private final List<SuggestionSource> args = new ArrayList<>();

        public Builder arg(String... choices) {
            args.add(new StaticSuggestionSource(List.of(choices)));
            return this;
        }

        public Builder argument(CommandArgument argument) {
            args.add(new ArgumentSuggestionSource(argument));
            return this;
        }

        public CommandCompletion build() {
            return new CommandCompletion(List.copyOf(args));
        }
    }

    private interface SuggestionSource {
        List<String> suggest(String partial);
    }

    private record StaticSuggestionSource(List<String> choices) implements SuggestionSource {
        @Override
        public List<String> suggest(String partial) {
            String query = partial == null ? "" : partial.toLowerCase();
            return choices.stream()
                .filter(value -> value.toLowerCase().startsWith(query))
                .toList();
        }
    }

    private static final class ArgumentSuggestionSource implements SuggestionSource {
        private final CommandArgument argument;

        private ArgumentSuggestionSource(CommandArgument argument) {
            this.argument = argument;
        }

        @Override
        public List<String> suggest(String partial) {
            return argument.suggest(partial);
        }
    }
}
