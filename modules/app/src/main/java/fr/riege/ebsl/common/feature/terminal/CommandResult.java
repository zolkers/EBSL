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

package fr.riege.ebsl.common.feature.terminal;

import java.util.Arrays;
import java.util.List;

public record CommandResult(boolean success, List<String> lines) {

    public static CommandResult ok(String... lines) {
        return new CommandResult(true, Arrays.asList(lines));
    }

    public static CommandResult ok(List<String> lines) {
        return new CommandResult(true, lines);
    }

    public static CommandResult error(String message) {
        return new CommandResult(false, List.of(message));
    }

    public static CommandResult noPlayer() {
        return error("Not connected to a server.");
    }

    public static CommandResult badUsage(String usage) {
        return error("Usage: " + usage);
    }
}
