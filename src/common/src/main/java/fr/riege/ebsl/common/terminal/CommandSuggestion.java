package fr.riege.ebsl.common.terminal;

public record CommandSuggestion(String fill, String hint) {
    public static CommandSuggestion of(String fill, String hint) {
        return new CommandSuggestion(fill, hint);
    }
}
