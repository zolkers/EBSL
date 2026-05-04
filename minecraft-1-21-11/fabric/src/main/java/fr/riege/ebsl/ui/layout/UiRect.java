package fr.riege.ebsl.ui.layout;

public record UiRect(int x, int y, int width, int height) {
    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public UiRect inset(int amount) {
        return new UiRect(x + amount, y + amount, Math.max(0, width - amount * 2), Math.max(0, height - amount * 2));
    }
}
