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

public record CommandContext(String[] args) {
    public CommandContext {
        args = args == null ? new String[0] : args.clone();
    }

    @Override
    public String[] args() {
        return args.clone();
    }

    public int argCount() {
        return args.length;
    }

    public String arg(int index) {
        return args[index];
    }

    public int argInt(int index) {
        return Integer.parseInt(args[index]);
    }

    public double argDouble(int index) {
        return Double.parseDouble(args[index]);
    }

    public CommandContext shift(int n) {
        String[] shifted = new String[Math.max(0, args.length - n)];
        System.arraycopy(args, n, shifted, 0, shifted.length);
        return new CommandContext(shifted);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CommandContext(String[] otherArgs) && Arrays.equals(args, otherArgs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(args);
    }

    @Override
    public String toString() {
        return "CommandContext[args=" + Arrays.toString(args) + ']';
    }
}
