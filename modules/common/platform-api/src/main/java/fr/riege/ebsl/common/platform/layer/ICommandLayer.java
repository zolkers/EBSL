/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.platform.layer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Provides the platform command bridge used by EBSL.
 *
 * <p>Implementations register commands, execute command input, emit formatted output, and optionally provide completions.</p>
 */
public interface ICommandLayer {
    /**
     * Handles one terminal command invocation.
     *
     * <p>Handlers receive a parsed command context and may provide parameters, completion rules, and nested subcommands.</p>
     */
    @FunctionalInterface
    interface CommandHandler {
        /**
         * Executes the operation represented by this contract.
 *
         * @param args the command or script arguments
         * @param output the callback used to emit command output
         */
        void execute(String[] args, Consumer<String> output);
    }

    /**
     * Registers the supplied value with this component.
 *
     * @param name the command or registry name
     * @param description the human-readable description
     * @param handler the handler to register
     */
    void register(String name, String description, CommandHandler handler);

    /**
     * Prints an informational message to the command output surface.
 *
     * @param message the message to display or record
     */
    default void print(String message) {
    }

    /**
     * Prints an error message to the command output surface.
 *
     * @param message the message to display or record
     */
    default void printError(String message) {
        print(message);
    }

    /**
     * Prints a success message to the command output surface.
 *
     * @param message the message to display or record
     */
    default void printSuccess(String message) {
        print(message);
    }

    /**
     * Returns completion suggestions for the supplied command input.
 *
     * @param input the raw input used for suggestion lookup
     * @return the requested values
     */
    default List<String> getSuggestions(String input) {
        return List.of();
    }
}
