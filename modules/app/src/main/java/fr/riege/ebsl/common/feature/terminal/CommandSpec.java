package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;

import java.util.*;

public final class CommandSpec implements CommandHandler {
    private final CommandMeta meta;
    private final CommandExecutor executor;
    private final CommandCompletion completion;
    private final List<GoalParameter> params;
    private final Map<String, CommandHandler> subcommands;

    private CommandSpec(CommandMeta meta,
                        List<CommandArgument> arguments,
                        CommandExecutor executor,
                        CommandCompletion completion,
                        List<GoalParameter> params,
                        Map<String, CommandHandler> subcommands) {
        this.meta = meta;
        this.executor = Objects.requireNonNull(executor, "command executor required");
        this.completion = completion != null ? completion : CommandCompletion.fromArguments(arguments);
        this.params = List.copyOf(params);
        this.subcommands = Map.copyOf(subcommands);
    }

    public static Builder named(String name) {
        return new Builder(name);
    }

    public CommandMeta meta() {
        return meta;
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        return executor.execute(ctx);
    }

    @Override
    public List<GoalParameter> params() {
        return params;
    }

    @Override
    public CommandCompletion completer() {
        return completion;
    }

    @Override
    public Map<String, CommandHandler> subcommands() {
        return subcommands;
    }

    @FunctionalInterface
    public interface CommandExecutor {
        CommandResult execute(CommandContext ctx);
    }

    public static final class Builder {
        private final String name;
        private String description = "";
        private String usage;
        private CommandScope scope = CommandScope.MC;
        private CommandExecutor executor;
        private CommandCompletion completion;
        private final List<CommandArgument> arguments = new ArrayList<>();
        private final List<GoalParameter> params = new ArrayList<>();
        private final Map<String, CommandHandler> subcommands = new LinkedHashMap<>();

        private Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("command name required");
            }
            this.name = name.toLowerCase();
        }

        public Builder description(String description) {
            this.description = description != null ? description : "";
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder scope(CommandScope scope) {
            this.scope = scope != null ? scope : CommandScope.MC;
            return this;
        }

        public Builder bothScopes() {
            return scope(CommandScope.BOTH);
        }

        public Builder terminalOnly() {
            return scope(CommandScope.TERMINAL);
        }

        public Builder mcOnly() {
            return scope(CommandScope.MC);
        }

        public Builder argument(CommandArgument argument) {
            if (argument != null) {
                arguments.add(argument);
            }
            return this;
        }

        public Builder argument(String name) {
            return argument(CommandArgument.named(name));
        }

        public Builder choices(String name, String... choices) {
            return argument(CommandArgument.choices(name, choices));
        }

        public Builder params(List<GoalParameter> params) {
            if (params != null) {
                this.params.addAll(params);
            }
            return this;
        }

        public Builder completion(CommandCompletion completion) {
            this.completion = completion;
            return this;
        }

        public Builder subcommand(String name, CommandHandler handler) {
            if (name != null && handler != null) {
                subcommands.put(name.toLowerCase(), handler);
            }
            return this;
        }

        public Builder executes(CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        public CommandSpec build() {
            String finalUsage = usage != null ? usage : defaultUsage();
            return new CommandSpec(
                new CommandMeta(name, description, finalUsage, scope),
                arguments,
                executor,
                completion,
                params,
                subcommands);
        }

        private String defaultUsage() {
            if (arguments.isEmpty()) {
                return name;
            }
            List<String> tokens = new ArrayList<>();
            tokens.add(name);
            for (CommandArgument argument : arguments) {
                tokens.add(argument.usageToken());
            }
            return String.join(" ", tokens);
        }
    }
}
