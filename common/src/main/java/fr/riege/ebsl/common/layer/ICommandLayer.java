package fr.riege.ebsl.common.layer;

import java.util.List;
import java.util.function.Consumer;

public interface ICommandLayer {
    @FunctionalInterface
    interface CommandHandler {
        void execute(String[] args, Consumer<String> output);
    }

    void register(String name, String description, CommandHandler handler);
    void print(String message);
    void printError(String message);
    void printSuccess(String message);
    List<String> getSuggestions(String input);
}
