package fr.riege.ebsl.terminal;

public record CommandSuggestion(String fill, String hint) {
    public static CommandSuggestion of(String fill, String hint) {
        return new CommandSuggestion(fill, hint);
    }
}
