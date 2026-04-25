package fr.riege.ebsl.botting.module;

public enum KeyDisplayAnchor {
    TOP_LEFT("Top left"),
    TOP_CENTER("Top center"),
    TOP_RIGHT("Top right"),
    BOTTOM_LEFT("Bottom left"),
    BOTTOM_CENTER("Bottom center"),
    BOTTOM_RIGHT("Bottom right");

    private final String label;

    KeyDisplayAnchor(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
