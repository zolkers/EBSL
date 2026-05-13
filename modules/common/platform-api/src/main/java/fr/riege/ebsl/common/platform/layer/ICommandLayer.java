package fr.riege.ebsl.common.platform.layer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the contract for {@code ICommandLayer} implementations.
 */
public interface ICommandLayer {
    /**
     * Defines the contract for {@code CommandHandler} implementations.
     */
    @FunctionalInterface
    interface CommandHandler {
        void execute(String[] args, Consumer<String> output);
    }

    void register(String name, String description, CommandHandler handler);

    default void print(String message) {
    }

    default void printError(String message) {
        print(message);
    }

    default void printSuccess(String message) {
        print(message);
    }

    default List<String> getSuggestions(String input) {
        return List.of();
    }
}
