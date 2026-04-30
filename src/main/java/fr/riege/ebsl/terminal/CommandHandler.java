package fr.riege.ebsl.terminal;

public interface CommandHandler {
    CommandResult execute(CommandContext ctx);

    default CommandCompletion completer() {
        return CommandCompletion.EMPTY;
    }
}
