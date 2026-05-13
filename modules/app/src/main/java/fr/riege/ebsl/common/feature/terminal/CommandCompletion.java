package fr.riege.ebsl.common.feature.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandCompletion {

    public static final CommandCompletion EMPTY = new CommandCompletion(List.of());

    private final List<SuggestionSource> argChoices;

    private CommandCompletion(List<SuggestionSource> argChoices) {
        this.argChoices = argChoices;
    }

    public List<String> suggest(int argIndex, String partial) {
        return suggest(List.of(), argIndex, partial);
    }

    public List<String> suggest(List<String> previousArgs, int argIndex, String partial) {
        if (argIndex < 0 || argIndex >= argChoices.size()) return List.of();
        return argChoices.get(argIndex).suggest(new Context(previousArgs, argIndex, partial));
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

        public Builder dynamic(ContextualSuggestionSource source) {
            args.add(new ContextualSuggestionSourceAdapter(source));
            return this;
        }

        public CommandCompletion build() {
            return new CommandCompletion(List.copyOf(args));
        }
    }

    public record Context(List<String> previousArgs, int argIndex, String partial) {
        public Context {
            previousArgs = previousArgs == null ? List.of() : List.copyOf(previousArgs);
            partial = partial == null ? "" : partial;
        }

        public String previousArg(int index) {
            return index >= 0 && index < previousArgs.size() ? previousArgs.get(index) : "";
        }
    }

    /**
     * Defines the contract for {@code ContextualSuggestionSource} implementations.
     */
    @FunctionalInterface
    public interface ContextualSuggestionSource {
        List<String> suggest(Context context);
    }

    /**
     * Defines the contract for {@code SuggestionSource} implementations.
     */
    private interface SuggestionSource {
        List<String> suggest(Context context);
    }

    private record StaticSuggestionSource(List<String> choices) implements SuggestionSource {
        @Override
        public List<String> suggest(Context context) {
            String query = context.partial().toLowerCase();
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
        public List<String> suggest(Context context) {
            return argument.suggest(context.partial());
        }
    }

    private record ContextualSuggestionSourceAdapter(ContextualSuggestionSource source) implements SuggestionSource {
        private ContextualSuggestionSourceAdapter {
            Objects.requireNonNull(source, "source");
        }

        @Override
        public List<String> suggest(Context context) {
            return source.suggest(context);
        }
    }
}
