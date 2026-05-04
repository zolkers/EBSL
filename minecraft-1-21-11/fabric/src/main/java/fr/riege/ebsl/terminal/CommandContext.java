package fr.riege.ebsl.terminal;

import net.minecraft.client.Minecraft;

public record CommandContext(Minecraft mc, String[] args) {

    public boolean hasPlayer() {
        return mc.player != null;
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
        return new CommandContext(mc, shifted);
    }
}
