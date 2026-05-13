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
        return other instanceof CommandContext that && Arrays.equals(args, that.args);
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
