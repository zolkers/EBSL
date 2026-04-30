package fr.riege.ebsl.general.module.overlay;

import fr.riege.ebsl.ui.layout.UiRect;

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

    public float x(UiRect viewport, float width, float pad) {
        return switch (this) {
            case TOP_LEFT,    BOTTOM_LEFT   -> viewport.x() + pad;
            case TOP_CENTER,  BOTTOM_CENTER -> viewport.x() + (viewport.width() - width) * 0.5f;
            case TOP_RIGHT,   BOTTOM_RIGHT  -> viewport.right() - width - pad;
        };
    }

    public float y(UiRect viewport, float height, float pad) {
        return switch (this) {
            case TOP_LEFT,    TOP_CENTER,    TOP_RIGHT    -> viewport.y() + pad;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> viewport.bottom() - height - pad;
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
